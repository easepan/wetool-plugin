## 接口参考文档

### 配置信息

获取用户的配置，第一个字符串类型的参数为`Alibaba Fast Json`中的`Path`：[路径语法参考](https://github.com/alibaba/fastjson/wiki/JSONPath)

```java
T config = WeUtils.getConfig().getConfig(String, Class<T>);
// 或者
Object config = WeUtils.getConfig().getConfig(String);
```

### Bean工厂

```java
// 注册单例的Bean
BeanFactory.register(T);

// 注册视图Bean（同样也是单例Bean），参数说明：tabId，tabName，视图控制器
BeanFactory.registerView(String, String, BaseViewController);

// 注册多例Bean，参数说明：beanName，Bean实例
BeanFactory.register(String, Object);
```

[更多方法请参考](src/main/java/org/code4everything/wetool/plugin/support/factory/BeanFactory.java)

### 事件中心

```java
// 注册一个事件：自定义一个事件唯一KEY，事件的订阅模式（单订阅、多订阅）
EventCenter.registerEvent(String eventKey, EventMode eventMode);

// 发布一个事件
EventCenter.publishEvent(String eventKey, Date eventTime);

// 订阅一个事件
EventCenter.subscribeEvent(String eventKey, EventHandler eventHandler);
```

内置事件

|事件名称|EventKey|备注|
|---|---|---|
|秒钟定时器|wetool_timer_seconds||
|100毫秒定时器|wetool_timer_100_ms||
|清除视图缓存|wetool_clear_fxml_cache||
|退出事件|wetool_exit||
|重启事件|wetool_restart|注意：该事件包括退出事件|
|主界面显示|wetool_show||
|主界面隐藏|wetool_hidden||
|剪贴板内容变化|event_clipboard_changed||
|触发角事件|event_mouse_corner_trigger|LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM|

### 数据库操作

数据源使用阿里巴巴 `Druid` 进行管理，调用类 `DruidSource` 获取用户配置的数据源

类 `JdbcExecutor` 提供简单的ORM映射

### 仅适用本插件库的特定工具类

[WeUtils](src/main/java/org/code4everything/wetool/plugin/support/util/WeUtils.java)

[FxUtils](src/main/java/org/code4everything/wetool/plugin/support/util/FxUtils.java)

```java
// 打开选项卡，参数说明：视图内容，自定义tabId，自定义tabName
FxUtils.openTab(Node, String, String);

// 获取当前运行的 TabPane
FxUtils.getTabPane();

// 获取当前运行的 Stage
FxUtils.getStage();

// 用系统默认软件打开文件
FxUtils.openFile(File);

// 加载视图，参数说明：WePluginSupporter实现类，视图在classpath中路径，是否缓存
FxUtils.loadFxml(Class<?>, String, boolean);

// 创建菜单
FxUtils.createMenuItem(String, EventHandler<ActionEvent>);
FxUtils.createMenuItem(String, ActionListener);

// 名称唯一的菜单，并添加至插件菜单
FxUtils.makePluginMenu(String)
```
  
[FxDialogs](src/main/java/org/code4everything/wetool/plugin/support/util/FxDialogs.java)

```text
FxDialogs.showDialog
FxDialogs.showChoice
FxDialogs.showTextInput
FxDialogs.showSuccess
FxDialogs.showInformation
FxDialogs.showError
FxDialogs.showException
```
