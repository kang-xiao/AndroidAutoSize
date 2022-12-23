# AndroidAutoSize

Android屏幕适配

### 各个类的作用描述：

** AutoSize ：用于屏幕适配的核心方法都在这里 **

** AutoSizeConfig ：屏幕适配的参数 **

** AutoAdaptStrategy （接口）：策略模式，方法：开始执行屏幕适配逻辑 **

** DefaultAutoAdaptStrategy ：默认模式，适配屏幕，用来处理已经无需适配的Class(需要实现CancelAdapt)和需要定制的Class（需要实现CustomAdapt） **

** WrapperAutoAdaptStrategy ：AutoAdaptStrategy装饰类，用来添加一些额外的职责 **

** DisplayMetricsInfo ：屏幕信息封装类 **

** onAdaptListener(接口) ：用于监听屏幕适配时的一些事件 **

** ActivityLifecycleCallbacksImpl ：用来替代在BaseActivity中加入适配代码的传统方式，这种方案类似于AOP,面向接口，侵入性低，方便统一管理，扩展性强，并且也支持适配三方库 **

** FragmentLifecycleCallbacksImpl ：用来替代在BaseFragment中加入适配代码的传统方式，这种方案类似于AOP,面向接口，侵入性低，方便统一管理，扩展性强，并且也支持适配三方库 **

** FragmentLifecycleCallbacksImplToAndroidx : 同上，用来适配Androidx **

** InitProvider ：应用启动前首先执行的ContentProvider，用于自动完成初始化 **

** utils/AutoSizeLog ：用来打印日志 **

** utils/AutoSizeUtils ： 常用工具类 **

** utils/Preconditions ： 常用判断类 **

** utils/ScreenUtils ：屏幕和状态栏相关工具类 **

** internal/CancelAdapt ： 放弃适配时需要实现该类 **

** internal/CustomAdapt ： 可自定义用于适配的一些参数 **

** unit/UnitsManager ：管理支持的所有单位，支持五种单位（dp、sp 、pt、in、mm）**

** unit/Subunits ：支持一些在Android系统上比较少见的单位作为副单位 **

** external/ExternalAdaptInfo ：用来存储外部三方库的适配参数 **

** external/ExternalAdaptManager ：管理三方库的适配信息和状态 **
