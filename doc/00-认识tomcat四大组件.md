容器
1. Engine： Catalina引擎
2. Host： 虚拟主机
3. context： 一个context代表一个web应用程序，配置context目的是指定对应webapp的根目录，还可以指定webapp额外属性，比如部署方式

服务
1. service：
    - 将connector连接器关联到引擎Engine

顶级组件
1. server
    - 一个运行在JVM中的实例

嵌套组件：
1. value： 请求到达webapp目录之前做一些拦截处理
    - access log value:
    - remote address filter value 基于IP做访问控制
2. logger:日志记录器

3. realm:关联一个认证库，实现认证和鉴权
    - UserDatabaseRealm
    - JDBCRealm
