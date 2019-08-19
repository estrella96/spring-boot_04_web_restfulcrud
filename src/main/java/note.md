# CRUD实例
## 1 引入资源

## 2 国际化
- 自适应浏览器的语言
- SpringMVC
    - 1 编写国际化配置文件
    - 2 使用ResourceBundleMessageSource管理国际化资源文件
    - 3 在页面使用fmt:message取出国际化内容
- SpringBoot
    - 1 编写国际化配置文件 抽取页面需要显示的国际化消息 
    - 2 SpringBoot自动配置好了管理国际化资源文件的组件
        MessageSourceAutoConfiguration.class
        配置 spring.messages.basename=i18n.login
    - 3 去页面获取国际化值 #{}获取
```html
   <!DOCTYPE html>
   <html lang="en"  xmlns:th="http://www.thymeleaf.org">
   	<head>
   		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
   		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
   		<meta name="description" content="">
   		<meta name="author" content="">
   		<title>Signin Template for Bootstrap</title>
   		<!-- Bootstrap core CSS -->
   		<!--@语法引入路径 前面加入webjars依赖-->
   		<link href="asserts/css/bootstrap.min.css" th:href="@{/webjars/bootstrap/4.0.0/css/bootstrap.css}" rel="stylesheet">
   		<!-- Custom styles for this template -->
   		<link href="asserts/css/signin.css" th:href="@{/asserts/css/signin.css}" rel="stylesheet">
   	</head>
   	<body class="text-center">
   		<form class="form-signin" action="dashboard.html" th:action="@{/user/login}" method="post">
   			<img class="mb-4" th:src="@{/asserts/img/bootstrap-solid.svg}" src="asserts/img/bootstrap-solid.svg" alt="" width="72" height="72">
   			<h1 class="h3 mb-3 font-weight-normal" th:text="#{login.tip}">Please sign in</h1>
   			<!--判断-->
   			<p style="color: red" th:text="${msg}" th:if="${not #strings.isEmpty(msg)}"></p>
   			<label class="sr-only" th:text="#{login.username}">Username</label>
   			<input type="text"  name="username" class="form-control" placeholder="Username" th:placeholder="#{login.username}" required="" autofocus="">
   			<label class="sr-only" th:text="#{login.password}">Password</label>
   			<input type="password" name="password" class="form-control" placeholder="Password" th:placeholder="#{login.password}" required="">
   			<div class="checkbox mb-3">
   				<label>
             			<input type="checkbox" value="remember-me"/> [[#{login.remember}]]
           		</label>
   			</div>
   			<button class="btn btn-lg btn-primary btn-block" type="submit" th:text="#{login.btn}">Sign in</button>
   			<p class="mt-5 mb-3 text-muted">© 2017-2018</p>
   			<a class="btn btn-sm" th:href="@{/index.html(l='zh_CN')}">中文</a>
   			<a class="btn btn-sm" th:href="@{/index.html(l='en_US')}">English</a>
   		</form>
   	</body>
   
   </html>
```
- 原理
    国际化Locale(区域信息对象)：LocaleResolver（获取区域信息对象）
    
    - 点击链接切换国际化
```java
/**
 * 可以在连接上携带区域信息 url(l="zh_CN")
 * MyLocaleResolver.class
 */
public class MyLocaleResolver implements LocaleResolver {
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String l = request.getParameter("l");
        Locale locale = Locale.getDefault();
        if(!StringUtils.isEmpty(l)){
            String[] split = l.split("_");
            locale = new Locale(split[0],split[1]);
        }
        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }
}
```
    配置类中注册组件
```java
    @Bean
    public LocaleResolver localeResolver(){

        return new MyLocaleResolver();
    }
```
## 3 登陆
- 模板引擎页面修改后要实时生效
    禁用模板引擎的缓存 spring.thymeleaf.cache=false 
    页面修改完成后ctrl+f9 重新编译
- 错误消息显示
```xml
<!--判断-->
    <p style="color: red" th:text="${msg}" th:if="${not #strings.isEmpty(msg)}"></p>
			
```
- 拦截器做登陆检查
    LoginHandlerInterceptor.class
```java
/**
 * 登录检查,
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {



    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        Object user = request.getSession().getAttribute("loginUser");
        if (user == null){
            //未登录,返回登录页面
            request.setAttribute("msg","没有权限请先登录");
            request.getRequestDispatcher("/index.html").forward(request,response);
            return false;
        }else {
            //已登录,放行请求
            return true;
        }
    }

}

```
拦截器配置
```java
 //所有的WebMvcConfigurerAdapter组件都会一起起作用
    @Bean //将组件注册在容器
    public WebMvcConfigurerAdapter webMvcConfigurerAdapter(){
        WebMvcConfigurerAdapter adapter = new WebMvcConfigurerAdapter() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("login");
                registry.addViewController("/index.html").setViewName("login");
                registry.addViewController("/main.html").setViewName("dashboard");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
//                super.addInterceptors(registry);
//                静态资源
//                SpringBoot已经做好静态资源映射
                registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("/**")
                        .excludePathPatterns("/index.html","/","/user/login");
            }
        };
        return adapter;
    }
```

## 4 要求
- RestfulCRUD：CRUD满足Restful要求
    - URI: /资源名称/资源标识
    - HTTP请求方式区分对资源CRUD操作 查emp-get 增emp-post 改emp/{id}-put 删emp/{id}-DELETE
- 实验请求架构
    操作          请求URI   请求方式
    查询所有员工    emps     GET
    查询某个员工    emp/{id} GET
    添加页面       emp      GET
    添加员工       emp      POST
    修改页面       emp/{id} GET
    修改员工       emp      PUT
    删除员工       emp/{id} DELETE
- 员工列表  
    bar.html 侧边栏
```html
<!--th:href="@{/emps} 发送的请求-->
 <li class="nav-item">
                <a class="nav-link" href="#" th:class="${activeUri=='emps'?'nav-link active':'nav-link'}" th:href="@{/emps}">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-users">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </svg>
                    员工管理
                </a>
            </li>
```
   - thymeleaf公共页面元素抽取
       
```html
1 抽取公共片段
<div th:fragment="copy">
重用片段
</div>

2 引入公共片段
<div th:insert="~{templatename::selector}"></div>
<div th:insert="~{templatename::fragmentname}"></div>
模板名：符合thymeleaf规则 只需要名字
可以不加~{} 行内写法加[[~{}]]

3 默认效果
insert 的功能片段在div标签中 会对格式有一定影响

```
   - th:insert 公共片段整个插入到引入的指定元素中
   - th:replace 将引入元素替换为公共片段
   - th:include 将被引入的片段内容包含进标签中
```html
<footer th:fragment="copy">
&copy; 2011 The Good Thymes Virtual Grocery
</footer>

引入方式：
<body> ...
<div th:insert="footer :: copy"></div> 
<div th:replace="footer :: copy"></div> 
<div th:include="footer :: copy"></div>
</body>

效果：
<body> ...
  <div>
    <footer>
&copy; 2011 The Good Thymes Virtual Grocery 
    </footer>
    </div>
    
<footer>
&copy; 2011 The Good Thymes Virtual Grocery
</footer>

<div>
&copy; 2011 The Good Thymes Virtual Grocery
  </div>
</body>
```  
   - 公共片段的高亮 8.2 Parameterizable fragment signatures
     引入片段时传入参数
   
```html
也可以不声明变量 但是替代时传参
<div th:fragment="frag (onevar,twovar)">
<p th:text="${onevar} + ' - ' + ${twovar}">...</p>
</div>

<div th:replace="::frag (${value1},${value2})">...</div>
<div th:replace="::frag (onevar=${value1},twovar=${value2})">...</div>
```
```html
引入
<div th:replace="commons/bar::topbar"></div>
<div th:replace="commons/bar::#sidbar(activeUri='main.html')"></div>

高亮
activeUri=='main.html' 加高亮 否则不加
主页面
<a class="nav-link active" href="#" th:class="${activeUri=='main.html'?'nav-link active':'nav-link'}" th:href="@{/main.html}">
员工管理
<a class="nav-link" href="#" th:class="${activeUri=='emps'?'nav-link active':'nav-link'}" th:href="@{/emps}">

在dashboard.html/list.html 引入sidbar时传入activeUri参数

```
   - 遍历取值
```html
<!--遍历emps-->
<tbody>
<tr th:each="emp:${emps}">
    <td th:text="${emp.id}"></td>
    <td th:text="${emp.lastName}"></td>
    <td th:text="${emp.email}"></td>
    <td th:text="${emp.gender}==0?'女':'男'"></td>
    <td th:text="${emp.department.departmentName}"></td>
    <td th:text="${#dates.format(emp.birth,'yyyy-MM-dd HH:mm:ss')}"></td>
    <td>
        <a class="btn btn-sm btn-primary" th:href="@{/emp/}+${emp.id}">编辑</a>

        <button th:attr="del_uri=@{/emp/}+${emp.id}" type="submit" class="btn btn-sm btn-danger deleteBtn">删除</button>
    </td>
</tr>
</tbody>
```

- 员工添加
    - 跳转到添加页面
```html
href="emp" th:href="@{/emp}
<h2><a class="btn btn-sm btn-success" href="emp" th:href="@{/emp}">员工添加</a></h2>
```
```java
//    来到员工添加界面 映射/emp请求
    @GetMapping("/emp")
    public String toAddPage(Model model){
//        来到添加页面,查出所有的部门,在页面显示
        Collection<Department> departments =
                departmentDao.getDepartments();
        model.addAttribute("depts",departments);
        return "emp/add";
    }
```
   - 添加页面 
```html
<form th:action="@{/emp}" method="post">
    <!--发送put请求修改员工数据-->
    <!--
    1、SpringMVC中配置HiddenHttpMethodFilter;（SpringBoot自动配置好的）
    2、页面创建一个post表单
    3、创建一个input项，name="_method";值就是我们指定的请求方式
    -->
    <input type="hidden" name="_method" value="put" th:if="${emp!=null}"/>
    <input type="hidden" name="id" th:if="${emp!=null}" th:value="${emp.id}">
    <div class="form-group">
        <label>LastName</label>
        <input name="lastName" type="text" class="form-control" placeholder="zhangsan" th:value="${emp!=null}?${emp.lastName}">
    </div>
    <div class="form-group">
        <label>Email</label>
        <input name="email" type="email" class="form-control" placeholder="zhangsan@atguigu.com" th:value="${emp!=null}?${emp.email}">
    </div>
    <div class="form-group">
        <label>Gender</label><br/>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="gender" value="1" th:checked="${emp!=null}?${emp.gender==1}">
            <label class="form-check-label">男</label>
        </div>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="gender" value="0" th:checked="${emp!=null}?${emp.gender==0}">
            <label class="form-check-label">女</label>
        </div>
    </div>
    <div class="form-group">
        <label>department</label>
        <!--提交的是部门的id-->
        <select class="form-control" name="department.id">
            <option th:selected="${emp!=null}?${dept.id == emp.department.id}" th:value="${dept.id}" th:each="dept:${depts}" th:text="${dept.departmentName}">1</option>
        </select>
    </div>
    <div class="form-group">
        <label>Birth</label>
        <input name="birth" type="text" class="form-control" placeholder="zhangsan" th:value="${emp!=null}?${#dates.format(emp.birth, 'yyyy-MM-dd HH:mm')}">
    </div>
    <button type="submit" class="btn btn-primary" th:text="${emp!=null}?'修改':'添加'">添加</button>
</form>
```
   - 添加功能实现 <form th:action="@{/emp}" method="post">
```java
//    添加员工
    //SpringMVC自动将请求参数和入参对象的属性进行一一绑定:要求请求参数的名字和javaBean入参的对象里面的属性名称对应
    //也就是页面中提交的表单的name属性与实体类中属性一一对应
    @PostMapping("/emp")
    public String addEmp(Employee employee){

        employeeDao.save(employee);
        //System.out.println("保存员工的信息"+employee.toString());
        //来到员工列表页面
        //redirect:表示重定向到一个地址  /代表当前项目路径
        //forward:表示转发到一个地址
        return "redirect:/emps";
    }
```
   - 提交的数据格式问题：生日 日期
    2017/12/12 默认方式
    2017-12-12
    2017.12.12
    日期格式化：SpringMVC将页面提交的值需要转换为指定的类型 
    配置： spring.mvc.date-format=yyyy-MM-dd HH:mm
- 修改员工
    - 点击编辑按钮 来到修改页面 回显员工信息
```html
<a class="btn btn-sm btn-primary" th:href="@{/emp/}+${emp.id}">编辑</a>
```
```java
    //来到修改页面,查出当前员工,在页面回显
    @GetMapping("/emp/{id}")
    //@PathVariable("id")获取路径变量
    public String toEditPage(@PathVariable("id") Integer id,Model model){
        Employee employee = employeeDao.get(id);
        model.addAttribute("emp",employee);

        Collection<Department> departments =
                departmentDao.getDepartments();
        model.addAttribute("depts",departments);
        //回到修改页面(修改添加二合一)
        return "emp/add";
    }
```
   - 修改put请求 与add页面合并
   th:value="${emp!=null}?${emp.lastName}"区分是修改还是添加
```html
<form th:action="@{/emp}" method="post">
    <!--发送put请求修改员工数据-->
    <!--
    1、SpringMVC中配置HiddenHttpMethodFilter;（SpringBoot自动配置好的）
    2、页面创建一个post表单
    3、创建一个input项，name="_method";值就是我们指定的请求方式
    if成立才会生成这个标签
    -->
    <input type="hidden" name="_method" value="put" th:if="${emp!=null}"/>
    <input type="hidden" name="id" th:if="${emp!=null}" th:value="${emp.id}">
    <div class="form-group">
        <label>LastName</label>
        <input name="lastName" type="text" class="form-control" placeholder="zhangsan" th:value="${emp!=null}?${emp.lastName}">
    </div>
    <div class="form-group">
        <label>Email</label>
        <input name="email" type="email" class="form-control" placeholder="zhangsan@atguigu.com" th:value="${emp!=null}?${emp.email}">
    </div>
    <div class="form-group">
        <label>Gender</label><br/>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="gender" value="1" th:checked="${emp!=null}?${emp.gender==1}">
            <label class="form-check-label">男</label>
        </div>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="gender" value="0" th:checked="${emp!=null}?${emp.gender==0}">
            <label class="form-check-label">女</label>
        </div>
    </div>
    <div class="form-group">
        <label>department</label>
        <!--提交的是部门的id-->
        <select class="form-control" name="department.id">
            <option th:selected="${emp!=null}?${dept.id == emp.department.id}" th:value="${dept.id}" th:each="dept:${depts}" th:text="${dept.departmentName}">1</option>
        </select>
    </div>
    <div class="form-group">
        <label>Birth</label>
        <input name="birth" type="text" class="form-control" placeholder="zhangsan" th:value="${emp!=null}?${#dates.format(emp.birth, 'yyyy-MM-dd HH:mm')}">
    </div>
    <button type="submit" class="btn btn-primary" th:text="${emp!=null}?'修改':'添加'">添加</button>
</form>
```
请求处理
```java
    //员工修改 需要提交id
    @PutMapping("/emp")
    public String updateEmployee(Employee employee){
//       System.out.println(employee);
        employeeDao.save(employee);
        return "redirect:/emps";
    }

```
- 员工删除
    Delete请求
```html
th:attr="del_uri=@{/emp/}+${emp.id}" 自定义属性
<button th:attr="del_uri=@{/emp/}+${emp.id}" type="submit" class="btn btn-sm btn-danger deleteBtn">删除</button>

<form id="deleteForm" method="post">
    <input type="hidden" name="_method" value="delete">
</form>

<script>
			 $(".deleteBtn").click(function () {
			     //删除当前员工的
			     $("#deleteForm").attr("action",$(this).attr("del_uri")).submit();
				 return false;
             });
			 
		 </script>
```
```java
    @DeleteMapping("/emp/{id}")
    public String deleteEmployee(@PathVariable("id") Integer id){

        employeeDao.delete(id);
        return "redirect:/emps";
    }

```

## 5 错误处理机制

- SpringBoot默认的错误处理机制
   - 默认效果
    1 浏览器 返回一个默认的错误界面
    2 如果是其他客户端 默认响应一个json数据
   - 原理
    SpringMVC自动配置
   org/springframework/boot/spring-boot-autoconfigure/2.1.0.RELEASE/spring-boot-autoconfigure-2.1.0.RELEASE.jar!/org/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration.class 
    错误处理自动配置
   org/springframework/boot/spring-boot-autoconfigure/2.1.0.RELEASE/spring-boot-autoconfigure-2.1.0.RELEASE.jar!/org/springframework/boot/autoconfigure/web/servlet/error/ErrorMvcAutoConfiguration.class
    ErrorMvcAutoConfiguration.class
    容器中添加了以下组件：
        1 DefaultErrorAttributes
```java
//帮我们在页面共享信息
public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
    Map<String, Object> errorAttributes = new LinkedHashMap();
    errorAttributes.put("timestamp", new Date());
    this.addStatus(errorAttributes, webRequest);
    this.addErrorDetails(errorAttributes, webRequest, includeStackTrace);
    this.addPath(errorAttributes, webRequest);
    return errorAttributes;
}
```
        2 BasicErrorController
```java
//处理默认的/error请求
@Controller
@RequestMapping({"${server.error.path:${error.path:/error}}"})
public class BasicErrorController extends AbstractErrorController {
     
    @RequestMapping(produces = {"text/html"})//产生html类型的数据 请求头识别为浏览器请求
        public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
            HttpStatus status = this.getStatus(request);
            Map<String, Object> model = Collections.unmodifiableMap(this.getErrorAttributes(request, this.isIncludeStackTrace(request, MediaType.TEXT_HTML)));
            response.setStatus(status.value());
            //去哪个页面 页面地址和内容
            ModelAndView modelAndView = this.resolveErrorView(request, response, status, model);
            return modelAndView != null ? modelAndView : new ModelAndView("error", model);
        }
    
        @RequestMapping//产生json数据 客户端请求
        public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
            Map<String, Object> body = this.getErrorAttributes(request, this.isIncludeStackTrace(request, MediaType.ALL));
            HttpStatus status = this.getStatus(request);
            return new ResponseEntity(body, status);
        }

}
```
        3 ErrorPageCustomizer     
```java
@Value("${error.path:/error}")
private String path = "/error";//系统出现错误之后来到error请求进行处理
```
        4 DefaultErrorViewResolver
```java
public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
    ModelAndView modelAndView = this.resolve(String.valueOf(status.value()), model);
    if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
        modelAndView = this.resolve((String)SERIES_VIEWS.get(status.series()), model);
    }

    return modelAndView;
}

private ModelAndView resolve(String viewName, Map<String, Object> model) {
    //默认SpringBoot可以去找到一个页面 error/404
    String errorViewName = "error/" + viewName;
    //模板引擎可以解析页面地址就用模板引擎解析
    TemplateAvailabilityProvider provider = this.templateAvailabilityProviders.getProvider(errorViewName, this.applicationContext);
    //模板引擎可用就返回errorViewName指定的视图地址
    //不可用 在静态资源文件夹下找errorViewName error/404.html
    return provider != null ? new ModelAndView(errorViewName, model) : this.resolveResource(errorViewName, model);
}
```
   - 步骤
    一旦系统出现4xx或5xx错误 
    ErrorPageCustomizer就会生效（定制错误的响应规则）来到/error请求
    /error请求被BasicErrorController处理
        响应页面解析：去哪个页面由DefaultErrorViewResolver解析
```java
protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status, Map<String, Object> model) {
    Iterator var5 = this.errorViewResolvers.iterator();

    ModelAndView modelAndView;
    do {
        if (!var5.hasNext()) {
            return null;
        }

        ErrorViewResolver resolver = (ErrorViewResolver)var5.next();
        modelAndView = resolver.resolveErrorView(request, status, model);
    } while(modelAndView == null);

    return modelAndView;
}
```
    
         

- 定制错误响应
    1 如何定制错误页面
        有模板引擎的情况下：templates/error/状态码 错误页面命名成 状态码.html
            4xx.html 5xx.html匹配所有错误
            页面能获取的信息
                timestamp:时间戳
                status:状态码
                error:错误提示
                exception:异常对象
                message:异常消息
                errors:JSR303数据校验错误
```html
					<h1>status:[[${status}]]</h1>
					<h2>timestamp:[[${timestamp}]]</h2>
```
        没有模板引擎：静态资源文件夹下找 static获取不到信息
        都没有：默认的错误提示页面
        
    2 如何定制错误json数据
    异常处理
```java
@ControllerAdvice
public class MyExceptionHandler {

    //没有自适应效果 浏览器和客户端出现异常的返回相同
//    @ResponseBody
//    @ExceptionHandler(UserNotExistException.class)
//    public  Map<String,Object> handleException(Exception e){
//        Map<String,Object> map = new HashMap<>();
//        map.put("code","user.notexist");
//        map.put("message",e.getMessage());
//        return map;
//    }

// 转发到/error是自适应的
    @ExceptionHandler(UserNotExistException.class)
    public  String  handleException(Exception e, HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        //传入自己的错误状态码 否则无法进入定制错误页面
        request.setAttribute("javax.servlet.error.status_code",400);
        map.put("code","user.notexist");
        map.put("message",e.getMessage());
        //转发到/error
        return "forward:/error";
    }
}
```
        携带定制数据：
            出现错误后来到/error请求 被BasicErrorController处理 
            响应出去可以获取的数据是由getErrorAttributes得到的（是AbstractErrorController规定的方法）
            
            1 完全来编写一个ErrorController的实现类（或者编写AbstractErrorController的子类）放在容器中
            2 页面上能用的数据或者json返回的数据是通过errorAttributes.getErrorAttributes得到的
                容器中DefaultErrorAttributes可以自己写类@Component继承重写getErrorAttributes在map中放入自定义信息
                RequestAttributes requestAttributes
                requestAttributes.getAttribute("name",0)//request携带的数据
                
            
    
        
     
    
    
    