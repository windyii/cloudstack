#!/usr/bin/env bash

usage() {
  printf "Usage: %s: -f <local path> -u <user> -h <remote host> -r <remote path> -y <password>\n" $(basename $0) >&2
}

fflag=
uflag=
hflag=
rflag=
yflag=

while getopts 'f:u:h:r:y:' OPTION
do
  case $OPTION in
  f)    fflag=1
                localfile="$OPTARG"
                ;;
  u)    uflag=1
                user="$OPTARG"
                ;;
  h)    hflag=1
                host="$OPTARG"
                ;;
  r)    rflag=1
                remotefile="$OPTARG"
                ;;
  y)    yflag=1
                password="$OPTARG"
                ;;
  ?)    usage
                exit 2
                ;;
  esac
done

if [ "$fflag" == "" -o  "$hflag" == "" -o "$rflag" == "" -o "$yflag" == "" ]; then
    usage
    exit 2
fi
if [ "$uflag" == "" ]; then
    user=`whoami`
fi

expect_cmds="
          set timeout -1
          spawn bbcp -v -4 -f -s 16 -w 8m $localfile $user@$host:$remotefile
          expect {
              "'"'"(yes/no)?"'"'"  {send "'"'"yes\r"'"'"; exp_continue}
              password:  {send "'"'"$password\r"'"'"; exp_continue}
              "'"'"Permission denied"'"'" {exit 3}
          }"

if [ ! -f $localfile ]; then
    echo "file doesn't exist"
    exit 1
fi

echo "$expect_cmds" | expect - 2>&1 >/dev/nul
ret=$?
if [ "$ret" == "0" ]
then
    echo "Success"
else
    echo "Failed to bbcp"
    exit $ret
fi
