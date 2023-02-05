package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    // 将页面传递过来的请求查询参数封装到自定义VO里，就不用一个个参数写了，同时springMVC会帮助封装进去，只要有相同属性
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        param.set_queryString(queryString);
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";  // 到templates下找list.html
    }
}
