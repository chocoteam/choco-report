package choco.invientcharts;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Window;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.IOException;

// Classe qui gère le côté serveur de l'application
public class InvientChartsServlet extends ApplicationServlet {

    private static final long serialVersionUID = -7058371518203040250L;

    @Override
    protected void writeAjaxPageHtmlVaadinScripts(Window window,
            String themeName, Application application, BufferedWriter page,
            String appUrl, String themeUri, String appId,
            HttpServletRequest request) throws ServletException, IOException {
        page.write("<script type=\"text/javascript\">\n");
        page.write("//<![CDATA[\n");
        page.write("document.write(\"<script language='javascript' src='"
                + request.getContextPath()
                + "/VAADIN/scripts/jquery-1.7.1.min.js'><\\/script>\");\n");
        page.write("document.write(\"<script language='javascript' src='"
                + request.getContextPath()
                + "/VAADIN/scripts/highcharts.js'><\\/script>\");\n");
        page.write("//]]>\n</script>\n");
        super.writeAjaxPageHtmlVaadinScripts(window, themeName, application,
                page, appUrl, themeUri, appId, request);
    }
}