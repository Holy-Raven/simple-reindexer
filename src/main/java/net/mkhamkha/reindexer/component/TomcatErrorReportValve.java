package net.mkhamkha.reindexer.component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;

import java.io.IOException;

@Slf4j
public class TomcatErrorReportValve extends ErrorReportValve {

    @Override
    protected void report(Request request, Response response, Throwable t) {

        if (response.getStatus() == HttpServletResponse.SC_BAD_REQUEST) {
            try {
                String message = "{\"error\":\"Некорректный запрос\",\"status\":400}";
                response.setHeader("Content-Type", "application/json; charset=utf-8");
                response.getWriter().write(message);

                if (t != null) { //на случай если response с HttpServletResponse.SC_BAD_REQUEST мы формируем сами где-то еще и тогда лог об этом пишем там же
                    log.error("Некорретный запрос {}", t.getMessage());
                }
            } catch (IOException e) {
                log.error("Ошибка формирования TomcatErrorReportValve. ", e);
            }
        }
    }
}
