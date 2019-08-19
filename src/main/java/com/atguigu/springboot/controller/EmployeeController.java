package com.atguigu.springboot.controller;

import com.atguigu.springboot.dao.DepartmentDao;
import com.atguigu.springboot.dao.EmployeeDao;
import com.atguigu.springboot.entities.Department;
import com.atguigu.springboot.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    DepartmentDao departmentDao;

//  查询所有员工返回列表页面
    @GetMapping("/emps")
    public String list(Model model){
        Collection<Employee> all = employeeDao.getAll();

        model.addAttribute("emps",all);
//        thymeleaf默认就会拼串
//        classpath:/templates/XXX.html
        return "emp/list";
    }

//    来到员工添加界面
    @GetMapping("/emp")
    public String toAddPage(Model model){
//        来到添加页面,查出所有的部门,在页面显示
        Collection<Department> departments =
                departmentDao.getDepartments();
        model.addAttribute("depts",departments);
        return "emp/add";
    }

//    添加员工
    //SpringMVC自动将请求参数和入参对象的属性进行一一绑定:要求请求参数的名字和javaBean入参的对象里面的属性名称对应
    @PostMapping("/emp")
    public String addEmp(Employee employee){

        employeeDao.save(employee);
        //System.out.println("保存员工的信息"+employee.toString());
        //来到员工列表页面
        //redirect:表示重定向到一个地址  /代表当前项目路径
        //forward:表示转发到一个地址
        return "redirect:/emps";
    }

    //来到修改页面,查出当前员工,在页面回显
    @GetMapping("/emp/{id}")
    public String toEditPage(@PathVariable("id") Integer id,Model model){
        Employee employee = employeeDao.get(id);
        model.addAttribute("emp",employee);

        Collection<Department> departments =
                departmentDao.getDepartments();
        model.addAttribute("depts",departments);
        //回到修改页面(修改添加二合一)
        return "emp/add";
    }

    //员工修改
    @PutMapping("/emp")
    public String updateEmployee(Employee employee){
//       System.out.println(employee);
        employeeDao.save(employee);
        return "redirect:/emps";
    }

    //员工删除

    @DeleteMapping("/emp/{id}")
    public String deleteEmployee(@PathVariable("id") Integer id){

        employeeDao.delete(id);
        return "redirect:/emps";
    }

}
