# 通过DSL标注代码实现逆向业务建模自动化

## 一、用dddplus-maven-plugin跑一遍代码

```bash
cd ${your java source code repo}
mvn io.github.dddplus:dddplus-maven-plugin:visualize \
    -DrootDir=${冒号分隔的目录名称} \
    -DpkgRef=${包级别交叉调用报告输出到哪一个文件, dot格式} \
    -DcallGraph=${核心方法调用关系报告输出到哪一个文件, dot格式} \
    -DplantUml=${业务模型输出到哪一个文件, svg格式} \
    -DtextModel=${业务模型输出到哪一个文件, txt格式}
```

例如：
```bash
mvn io.github.dddplus:dddplus-maven-plugin:visualize \
    -DrawClassSimilarity=true \
    -DsimilarityThreshold=88 \
    -DrootDir=application:domain:web \
    -DpkgRef=doc/pkgref.dot \
    -DcallGraph=doc/callgraph.dot \
    -DplantUml=doc/myapp.svg \
    -DtextModel=doc/myapp.txt
```

执行后，就可以在doc目录下看到一些自动生成的报告。例如，在`doc/myapp.txt`文件，会看到：
- DSL标注覆盖度
- 代码规模：多少个类，多少个方法，多少个属性，多少条语句
- 规模最大的方法 top 10
- 相似度超过`88%`的相似类

但此时由于还未标注，生成的业务模型`doc/myapp.svg`只有基础的汇总信息，看不到模型。call graph也是空的，因为我们只关注人工标注的方法。

## 二、开始DSL标注

### 2.1 引入pom依赖

在需要标注的代码模块都需要引入`dddplus-spec`依赖：可以放心使用，它不会引入任何间接依赖。

```xml
<dependency>
    <groupId>io.github.dddplus</groupId>
    <artifactId>dddplus-spec</artifactId>
</dependency>
```

### 2.2 开始DSL标注

请参考[DSL javadoc](https://funkygao.github.io/cp-ddd-framework/doc/apidocs/io/github/dddplus/dsl/package-summary.html)。

#### 2.2.1 从 `@Aggregate` 开始标注

逆向建模自动生成的业务模型，是以`Aggregate`为边界进行组织的，因此先划分边界。

`Aggregate`与`DDD`里的聚合概念一致，是业务边界；非DDD项目可以理解为模块。

具体地，在某个`package`下创建`package-info.java`，例如：

```java
@Aggregate(name = "复核报差异")
package ddd.plus.showcase.wms.domain.diff;
```

#### 2.2.2 标注核心实体类

请参考我们提供的[Carton示例](../dddplus-test/src/test/java/ddd/plus/showcase/wms/domain/carton/Carton.java)。

这涉及到的DSL包括：
- KeyElement 关键业务字段
- KeyRelation 业务实体间重要关系
- KeyBehavior 该实体的关键业务行为
- KeyRule 该实现的关键业务规则

#### 2.2.3 标注服务与流程片段类

贫血模型往往把业务逻辑写到各个`Service`/`Worker`/`Utils`/`Validator`/`Processor`等类里，这不符合面向对象思想。

通过`@KeyFlow`可以把行为职责重新分配到业务实体上，提升模型可理解性，例如：

```java
public class FooService {

    @KeyFlow(actor = Order.class) // actor是行为主体的意思
    public void doSth(Order order) {
    }
}
```

有时候，目前代码里并不存在一个恰当的业务概念抽象，在逆向过程中发现了它，但重构工作量太大不敢做，这时候可以先让这个业务概念仅存在于逆向模型里。

具体办法是为该业务概念创建一个类，实现`IVirtualModel`接口，相关的行为职责通过`KeyFlow#actor`赋予到这个类上。

#### 2.2.4 标注业务入口

业务入口，通常位于`RPC接口`/`MQ Consumer`/`Controller`等类，在这些类的关键方法上标注`@KeyUsecase`，例如：

```java
public class CheckingController {
    // 提交复核任务
    @KeyUsecase
    public ApiResponse<String> submitTask(@Valid SubmitTaskDto dto) { }
}
```

#### 2.2.5 标注业务事件

通过`@KeyEvent`标注在业务事件类上，例如：

```java
@KeyEvent
public class OrderShippedEvent {
}
```

#### 2.2.6 标注建议

标注过程，不要一次性做全，可以做一点看一点，试探性摸索，熟悉后再做彻底标注。

## 三、DSL标注后执行dddplus-maven-plugin

在标注过程中，可以随时执行`dddplus-maven-plugin`，以观察逆向业务模型的变化。

## 四、最佳实践

为了与日常开发、设计评审、代码评审等结合起来，建议把逆向生成的模型文件纳入版本控制，从而完整追溯业务模型的演进过程。