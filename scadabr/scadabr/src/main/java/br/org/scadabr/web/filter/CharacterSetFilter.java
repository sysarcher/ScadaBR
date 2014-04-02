/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author aploese
 */
public class CharacterSetFilter implements Filter {
  private String charset;
  
  @Override
  public void init(FilterConfig filterConfig)
  {
    charset = filterConfig.getInitParameter("charset");
  }
  
  @Override
  public void destroy() {}
  
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
    throws IOException, ServletException
  {
    if (servletRequest.getCharacterEncoding() == null) {
      servletRequest.setCharacterEncoding(charset);
    }
    servletResponse.setCharacterEncoding(charset);
    filterChain.doFilter(servletRequest, servletResponse);
  }
}