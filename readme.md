About
canal [kə'næl],主要用途是用于 MySQL 数据库增量日志数据的订阅、消费和解析，是阿里巴巴开发并开源的，采用Java语言开发;历史背景是早期阿里巴巴因为杭州和美国双机房部署，存在跨机房数据同步的业务需求，实现方式主要是基于业务 trigger（触发器） 获取增量变更。从2010年开始，阿里巴巴逐步尝试采用解析数据库日志获取增量变更进行同步，由此衍生出了canal项目.

使用Maven的assembly插件实现自定义打包;可以实现
1、配置文件与源码分开 
2、项目依赖的jar包放到指定位置，同时把项目本身打成jar包

部署操作：
1、需要修改logback-spring.xml文件中的日志存放位置
  <property name="logging.path" value="H:/hxyc-workspace/logs" />
2、修改druid.properties配置文件;数据库的连接信息
3、修改properties.properties配置文件;修改canal服务器的信息
4、启动项目只能通过bin目录下相关脚本来启动；修改的配置文件才会生效 
