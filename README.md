# quant

标签（空格分隔）： 股票 量化交易 

---

[TOC]

# 一、 工程目录结构介绍
##1. assets目录
### keys 
> 加密通信时使用的密钥存储位置. 暂时用不到. 忽略它

### query_history 
> 保存以往选股历史

### stockHistories
> 保存股票从2010年以来的k线数据 `java Serializable自动提供的数据格式`

### web
> 忽略它

###CreateStockTable.sql
> 用于写入SqlServer的sql语句模板

###pref.txt
> 当前的选股设置

###rules.syntax
> 公式解释器的语法文件

##2. db目录
> 存放sqlite的数据库

##3. libs目录
> 引用的库

##4. model目录
> trufun uml建模工具的模型文件

##5. src目录
> 项目的源代码目录

- ssq.stock包 
>与股票的更新, 存储和计算相关的基础类

- ssq.stock.analyser 包 
> 提供易用的的全遍历抽象基类和几个简单的实现(sqlserver更新器, 历史数据文本输出器等). 由于历史原因, 采用了"分析器"这个名称, 实际上就是一个多线程遍历的基类

- ssq.stock.gui包
> 为选股而做的图形界面. 也提供了比较易用的基类: 带状态条的窗体, 表格窗体, 树形窗体等
> 也放入了生成K线图的类, 从网上下载的, 还没有使用过

- ssq.stock.interpreter包
> 公式解释器及其工具类 使用runcc做的解释器, 动态生成抽象语法树

- ssq.utils
> 工具类 不一定都在本项目中用到

- ssq.utils.taskdistributer
> 多线程作业调度需要的类

##6. tmp目录
> 临时输出目录

##7. .classpath & .project
> eclipse java工程的clsaapath和项目文件

##8. .gitignore
> git版本库的忽略列表

##9. README.md
> 本文

##10. SelectStock.jar
> 导出的可执行文件 安装[JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)或者JRE8后可以运行 
> 使用的库文件与libs里的相同, 将libs改名为SelectStock_lib才可以正确地找到依赖库

##11. sendkey.exe
> 使用东方财富网的windows客户端
打开东方财富网后, 在本软件的选股结果列表里边双击股票, 将在东方财富网的客户端里自动跳转到对应的股票. 
这个exe文件便是执行跳转的程序

#二、公式选股器的使用
> 主要是公式的编写. 

-我已经在发布的程序中提供了一个示例. 
```
/*这样的都是注释, 去掉后才能执行*/
max(250->125).opening/*开盘价*/..norest/*以不复权价格计算(省略这一项将以后复权价格计算)*/ 
/*上边一行是一个函数计算的结果. 它的意思是, 最近250天到最近125天的以不复权价格计算的开盘价的最大值*/
* 2 /*支持四则运算构成的表达式*/
/*每个函数执行都得到一个数值, 普通的数值也是数值, 数值的四则运算仍得到一个数值*/
< /*小于号*/
average(5->1).highest /*这里就没有使用"..norest", 用复权价格计算最近5天最高价的平均值*/ 
/*以上的所有, 构成了一个不等式. 每个不等式就是待评分的最基础项. 把每个股票的历史数据代入这个不等式, 计算左右两个表达式, 都能得到两个数值, 这两个数值对这个不等式的满足程度作为评分标准*/
@2 /*权重因子, 不加默认为1, 是本不等式分值不满时的扣分倍率*/ 
/*所有前边的这一堆是一个公式复合的基本单元. 公式支持用 &&, ||和() 嵌套地表达*/
&& 
(
3<4 /*这个没啥用, 只是用来说明, 只要是不等式就可以出现*/
|| 
sum(250->1).quantity /*最近250天的成交量之和*/ > 10000000000)
/*先算&&再算||, 括号的优先级更高*/
```

这个实例需要去掉注释后放入程序中运行
```
max(250->125).opening..norest * 2 < average(5->1).highest @2 && (3<4 || sum(250->1).quantity > 10000000000)
```

这只是个示例, 用这个公式选出来什么垃圾股跟我没关系~

#三、关于接口

##1. java调C
> 可以用命令行简单地实现

##2. java调matlab
> 使用javabuilder库

##3. 为SPSS提供数据源
1. 使用sqlserver2005
>将sa账户的密码设置为00, 或者修改我的源代码, 都可以使用SqlserverUpdater将历史数据放入sqlserver. 
>注意: jdbc是连接到具体的数据库上的, 所以**要先用sqlserver自带的SQL Server Management Studio建立一个名为Stock的数据库**

2. 使用文本文件
>使用TextOutPuter可以输出所有股票的历史数据