package com.hialan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Alan
 * Email:alan@hialan.com
 * Date: 4/5/15 03:21
 */
@Controller
public class HomeController {
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		System.out.println("Welcome home!");

		return "home";
	}
}
