#!/usr/bin/env sh
# 测试 Process 是持续输出，还是脚本最终执行完后，统一输出
for i in {1..3} ; do
    sleep $i
    echo $i
done
