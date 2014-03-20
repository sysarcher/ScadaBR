/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.util;

import br.org.scadabr.ImplementMeException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;

/**
 *
 * @author aploese
 */
public abstract class PaginatedListController {

    protected abstract PaginatedData getData(HttpServletRequest request, PagingDataForm paging, BindException errors) throws Exception;

    protected Object getCommand(HttpServletRequest request) {
        throw new ImplementMeException();
    }

}
