package ch.qos.logback.access.jetty;

import java.io.File;
import java.util.Iterator;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Response;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This class is logback's implementation of jetty's RequestLog interface.
 * <p>
 * It can be seen as logback classic's LoggerContext. Appenders can be attached
 * directly to RequestLogImpl and RequestLogImpl uses the same StatusManager as
 * LoggerContext does. It also provides containers for properties.
 * <p>
 * To configure jetty in order to use RequestLogImpl, the following lines must
 * be added to the jetty configuration file:
 * <pre>
 * &lt;Ref id="requestLog"&gt; 
 *   &lt;Set name="requestLog"&gt; 
 *     &lt;New id="requestLogImpl" class="ch.qos.logback.access.jetty.RequestLogImpl"&gt;&lt;/New&gt;
 *   &lt;/Set&gt; 
 * &lt;/Ref&gt;
 * </pre>
 * By default, RequestLogImpl looks for a logback configuration file called
 * logback.xml, in the same folder where jetty.xml is located, that is
 * /etc/logback.xml. The logback.xml file is slightly different than the usual
 * logback classic configuration file. Most of it is the same: Appenders and
 * Layouts are declared the exact same way. However, loggers elements are not
 * allowed.
 * <p>
 * Here is a sample logback.xml file that can be used right away:
 * <pre>
 * &lt;configuration&gt; 
 *   &lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"&gt; 
 *     &lt;layout class="ch.qos.logback.access.PatternLayout"&gt; 
 *       &lt;param name="Pattern" value="%date %server %remoteIP %clientHost %user %requestURL %post" /&gt;
 *     &lt;/layout&gt; 
 *   &lt;/appender&gt; 
 *   
 *   &lt;appender-ref ref="STDOUT" /&gt; 
 * &lt;/configuration&gt;
 * </pre>
 * A special, module-specific implementation of PatternLayout was implemented to allow
 * http-specific patterns to be used. The {@link ch.qos.logback.access.PatternLayout} provides
 * a way to format the logging output that is just as easy and flexible as the usual 
 * PatternLayout.
 * For more information about the general use of a PatternLayout, please refer to logback
 * classic's {@link ch.qos.logback.classic.PatternLayout}. For information about logback
 * access' specific PatternLayout, please refer to it's javadoc.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class RequestLogImpl extends ContextBase implements RequestLog,
    AppenderAttachable {

  public final static String DEFAULT_CONFIG_FILE = "etc" + File.separatorChar
      + "logback.xml";

  AppenderAttachableImpl aai = new AppenderAttachableImpl();
  String filename;

  public void log(Request jettyRequest, Response jettyResponse) {
    JettyServerAdapter adapter = new JettyServerAdapter(jettyRequest,
        jettyResponse);
    AccessEvent accessEvent = new AccessEvent(jettyRequest, jettyResponse,
        adapter);
    // TODO better exception handling
    aai.appendLoopOnAppenders(accessEvent);
  }

  public void start() throws Exception {
    if (filename == null) {
      String jettyHomeProperty = System.getProperty("jetty.home");

      filename = jettyHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
      getStatusManager().add(
          new ErrorStatus("filename property not set. Assuming [" + filename
              + "]", this));

    }
    File configFile = new File(filename);
    if (configFile.exists()) {
      JoranConfigurator jc = new JoranConfigurator();
      jc.setContext(this);
      jc.doConfigure(filename);
      StatusPrinter.print(getStatusManager());
    } else {
      getStatusManager().add(
          new ErrorStatus("[" + filename + "] does not exist", this));
    }

  }

  public void stop() throws Exception {
    // System.out.println("RequestLogImpl-stop called");
    aai.detachAndStopAllAppenders();
  }

  public boolean isRunning() {
    // System.out.println("RequestLogImpl-isRunning called");
    return false;
  }

  public void setFileName(String filename) {
    this.filename = filename;
  }

  public boolean isStarted() {
    // System.out.println("RequestLogImpl-isStarted called");
    return true;
  }

  public boolean isStarting() {
    // System.out.println("RequestLogImpl-isStarting called");
    return false;
  }

  public boolean isStopping() {
    // System.out.println("RequestLogImpl-isStopping called");
    return false;
  }

  public boolean isFailed() {
    // System.out.println("RequestLogImpl-isFailed called");
    return false;
  }

  public void addAppender(Appender newAppender) {
    aai.addAppender(newAppender);
  }

  public Iterator iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  public Appender getAppender(String name) {
    return aai.getAppender(name);
  }

  public boolean isAttached(Appender appender) {
    return aai.isAttached(appender);
  }

  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();

  }

  public boolean detachAppender(Appender appender) {
    return aai.detachAppender(appender);
  }

  public Appender detachAppender(String name) {
    return aai.detachAppender(name);
  }
}
