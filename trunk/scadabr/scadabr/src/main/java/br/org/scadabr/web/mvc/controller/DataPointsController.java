/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author aploese
 */
@Controller
@Scope("request")
@RequestMapping(value = "/dataPoints")
public class DataPointsController {

    @RequestMapping(method = RequestMethod.GET)
    public String showForm() {
        return "dataPoints";
    }

}
 
