# IPMI-Java Proof Of Concepts

本项目基于[nextian ipmi java](https://nextian.com/product/tools/ipmi-java/) 的实现，保留了核心通信部分，以及基板状态查询命令。

运行方法：

1. 确定支持 IPMI 请求的 IP 地址，以及管理员账号和密码。可以参考 [vapor-ware/ipmi-simulator: ipmi_sim in a minimal Docker container (github.com)](https://github.com/vapor-ware/ipmi-simulator)
2. 使用 IDEA 运行 `src/main/java/com/nextian/ipmi` 目录下的 `RunGetChassisStatus` 的主函数，并以 IP、管理员账号、管理员密码作为命令行参数；或者使用 Maven 工具打包后直接运行字节码。

惠普服务器验证结果：
```bash
Connector created
Connection created
Session open
Received answer
System power state is down
Session closed
Connection manager closed

Process finished with exit code 0
```

ipmi simulator 验证结果：
```bash
Connector created
Connection created
Session open
Received answer
System power state is up
Session closed
Connection manager closed

Process finished with exit code 0
```