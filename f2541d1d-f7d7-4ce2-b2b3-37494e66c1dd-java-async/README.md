# 发送短信完整工程示例

该项目为SendSms的完整工程示例。

**工程代码建议使用更安全的无AK方式，凭据配置方式请参阅：[管理访问凭据](https://help.aliyun.com/zh/sdk/developer-reference/java-asynchronous-sdk-manage-access-credentials)。**

## 运行条件

- 下载并解压需要语言的代码;

- *最低要求Java 8*

## 执行步骤

完成凭据配置后，可以在**解压代码所在目录下**按如下的步骤执行：

```sh
mvn clean package
mvn exec:java -Dexec.mainClass=demo.SendSms -Dexec.cleanupDaemonThreads=false
```

## 使用的 API

-  SendSms：向指定的手机号码发送短信。 更多信息可参考：[文档](https://next.api.aliyun.com/document/Dysmsapi/2017-05-25/SendSms)

## API 返回示例

*下列输出值仅作为参考，实际输出结构可能稍有不同，以实际调用为准。*


- JSON 格式 
```js
{
  "Code": "OK",
  "Message": "OK",
  "BizId": "9006197469364984****",
  "RequestId": "F655A8D5-B967-440B-8683-DAD6FF8DE990"
}
```

