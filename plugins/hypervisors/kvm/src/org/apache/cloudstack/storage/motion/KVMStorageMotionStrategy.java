package org.apache.cloudstack.storage.motion;

import java.util.Map;

import javax.inject.Inject;

import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionStrategy;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.StrategyPriority;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.command.FastCopyCommand;
import org.apache.cloudstack.storage.to.PrimaryDataStoreTO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.configuration.Config;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage;
import com.cloud.storage.StoragePool;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.template.TemplateManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.VirtualMachineManager;

public class KVMStorageMotionStrategy implements DataMotionStrategy {
    private static final Logger s_logger = Logger.getLogger(KVMStorageMotionStrategy.class);
    @Inject
    EndPointSelector _selector;
    @Inject
    HostDao _hostDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    VolumeDao _volDao;
    @Inject
    TemplateManager _templateMgr;
    @Inject
    VMTemplateDao _tmpltDao;
    @Inject
    DataStoreManager _dataStoreMgr;
    @Inject
    AgentManager _agentMgr;

    @Override
    public StrategyPriority canHandle(DataObject srcData, DataObject destData) {
        if (srcData.getType() == DataObjectType.VOLUME && destData.getType() == DataObjectType.VOLUME
                && srcData.getDataStore().getRole() == DataStoreRole.Primary && destData.getDataStore().getRole() == DataStoreRole.Primary) {
            PrimaryDataStoreTO priDataStore =(PrimaryDataStoreTO) srcData.getDataStore().getTO();
            String hostPublicIp = priDataStore.getHost();
            HostVO host = _hostDao.findByPublicIp(hostPublicIp);
            if (host.getHypervisorType() == HypervisorType.KVM) {
                s_logger.debug(this.getClass() + " can handle the request because the hosts have KVM hypervisor");
                return StrategyPriority.HYPERVISOR;
            }
        }
        return StrategyPriority.CANT_HANDLE;
    }

    @Override
    public StrategyPriority canHandle(Map<VolumeInfo, DataStore> volumeMap, Host srcHost, Host destHost) {
        if (srcHost.getHypervisorType() == HypervisorType.KVM && destHost.getHypervisorType() == HypervisorType.KVM) {
            s_logger.debug(this.getClass() + " can handle the request because the hosts have KVM hypervisor");
            return StrategyPriority.HYPERVISOR;
        }
        return StrategyPriority.CANT_HANDLE;
    }

    @Override
    public Void copyAsync(DataObject srcData, DataObject destData, Host destHost, AsyncCompletionCallback<CopyCommandResult> callback) {
        Answer answer = null;
        String errMsg = null;
        try {
            s_logger.debug("copyAsync inspecting src type " + srcData.getType().toString() + " copyAsync inspecting dest type " + destData.getType().toString());

            if (destData.getType() == DataObjectType.VOLUME && srcData.getType() == DataObjectType.VOLUME &&
                srcData.getDataStore().getRole() == DataStoreRole.Primary && destData.getDataStore().getRole() == DataStoreRole.Primary) {

                VolumeObjectTO srcVolumeTO = (VolumeObjectTO)srcData.getTO();
                VolumeObjectTO destVolumeTO = (VolumeObjectTO)destData.getTO();
                if (srcVolumeTO.getVolumeType() == Volume.Type.ROOT) {
                    //prepare template on primary storage for root volume.
                    VolumeVO srcVolume = _volDao.findById(srcVolumeTO.getVolumeId());
                    VMTemplateVO template = _tmpltDao.findById(srcVolume.getTemplateId());
                    VolumeVO destVolume = _volDao.findById(destVolumeTO.getVolumeId());
                    if (template.getFormat() != Storage.ImageFormat.ISO) {
                        s_logger.info("Start to preload template " + template.getId() + " into primary storage " + destVolume.getPoolId());
                        StoragePool storagePool = (StoragePool)_dataStoreMgr.getPrimaryDataStore(destVolume.getPoolId());
                        _templateMgr.prepareTemplateForCreate(template, storagePool);
                        s_logger.info("End of preloading template " + template.getId() + " into primary storage " + storagePool.getId());
                    }
                }

                PrimaryDataStoreTO priDataStore = (PrimaryDataStoreTO) srcData.getDataStore().getTO();
                String hostPublicIp = priDataStore.getHost();
                HostVO srcHostVO = _hostDao.findByPublicIp(hostPublicIp);
                priDataStore =(PrimaryDataStoreTO) destData.getDataStore().getTO();
                hostPublicIp = priDataStore.getHost();
                HostVO destHostVO = _hostDao.findByPublicIp(hostPublicIp);

                _hostDao.loadDetails(destHostVO);

                answer = copyVolumeBetweenPools(srcData, destData, srcHostVO, destHostVO);
            }
            if (answer != null && !answer.getResult()) {
                errMsg = answer.getDetails();
            }
        } catch (Exception e) {
            s_logger.debug("copy failed", e);
            errMsg = e.toString();
        }
        CopyCommandResult result = new CopyCommandResult(null, answer);
        result.setSuccess(answer == null ? false : answer.getResult());
        result.setResult(errMsg);
        callback.complete(result);
        return null;
    }

    @Override
    public Void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CopyCommandResult> callback) {
        return null;
    }

    @Override
    public Void copyAsync(Map<VolumeInfo, DataStore> volumeMap, VirtualMachineTO vmTo, Host srcHost, Host destHost,
            AsyncCompletionCallback<CopyCommandResult> callback) {
        return null;
    }

    private Answer copyVolumeBetweenPools(DataObject srcData, DataObject destData, HostVO srcHostVO, HostVO destHostVO) {
        String value = _configDao.getValue(Config.CopyVolumeWait.key());
        int _copyvolumewait = NumbersUtil.parseInt(value, Integer.parseInt(Config.CopyVolumeWait.getDefaultValue()));
        FastCopyCommand cmd = new FastCopyCommand(srcData.getTO(), destData.getTO(), _copyvolumewait, VirtualMachineManager.ExecuteInSequence.value());
        cmd.setDestHostPassword(destHostVO.getDetail("password"));
        Answer answer = _agentMgr.easySend(srcHostVO.getId(), cmd);
        if (answer == null || !answer.getResult()) {
            if (answer != null) {
                s_logger.debug("copy to primary store failed: " + answer.getDetails());
            }
            return answer;
        } else {
            VolumeVO volumeVo = _volDao.findById(destData.getTO().getId());
            volumeVo.setPath(srcData.getTO().getPath());
            _volDao.update(volumeVo.getId(), volumeVo);
        }

        return answer;
    }

}
