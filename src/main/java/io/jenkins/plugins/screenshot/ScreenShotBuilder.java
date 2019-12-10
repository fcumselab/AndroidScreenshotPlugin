package io.jenkins.plugins.screenshot;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ScreenShotBuilder extends Builder implements SimpleBuildStep {
  private final String projectName;
  String filePath = "D:\\Temp\\AndroidManifest.xml";

  @DataBoundConstructor
  public ScreenShotBuilder(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectName() {
    return this.projectName;
  }

  public String getPackageName(Document doc) {
    String packageName = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getNodeValue();
    return packageName;
  }

  public ArrayList<String> paserXML(Document doc)  throws ParserConfigurationException, IOException, SAXException {
    int avtivityNumber = doc.getElementsByTagName("activity").getLength();
    ArrayList<String> activityNames = new ArrayList<String>();
    String mainActivity = null;
    for(int i = 0; i < avtivityNumber; i++){
      Node activity = doc.getElementsByTagName("activity").item(i);
      String activityName = activity.getAttributes().getNamedItem("android:name").getNodeValue();

      activityName = activityName.replace(".", "");
      activityNames.add(activityName);
      System.out.println(activityName);
//      // Find main activity name
//      NodeList children = activity.getChildNodes();
//      for(int j=0 ; j < children.getLength(); j++){
//        if(children.item(j).getNodeType() == Node.ELEMENT_NODE &&
//                activity.getChildNodes().item(j).getNodeName().equals("intent-filter")){
//          mainActivity = activityName;
//        }
//      }
    }
    return activityNames;
  }

  public static void generateTestFile(String packageName, ArrayList<String> activitys) throws FileNotFoundException, UnsupportedEncodingException {
    PrintWriter writer = new PrintWriter("D:\\Temp\\SpoonTest.java", "UTF-8");
    // package
    writer.println("package " + packageName + ";");

    /**
     * Geberate Import
     */
    writer.println("import android.content.Intent;\n" +
            "import androidx.test.ext.junit.runners.AndroidJUnit4;\n" +
            "import androidx.test.rule.ActivityTestRule;\n" +
            "import com.squareup.spoon.Spoon;\n" +
            "import org.junit.Rule;\n" +
            "import org.junit.Test;\n" +
            "import org.junit.runner.RunWith;\n");
    /**
     * Geberate class
     */
    writer.println("@RunWith(AndroidJUnit4.class)");
    writer.println("public class SpoonTest {");

    /**
     * Geberate Screenshot Code
     */
    for(int i = 0;i < activitys.size(); i++){
      String activityRule = "myActivityRule" + i;
      writer.println("@Rule");
      writer.println("public ActivityTestRule<" + activitys.get(i) + ">" + activityRule);
      writer.println("    = new ActivityTestRule(" +  activitys.get(i) + ".class);" );
      writer.println("@Test");
      writer.println("public void spoonTest" + i + "() {");
      writer.println("  " + activityRule + ".launchActivity(new Intent());" );
      writer.println("Spoon.screenshot(" + activityRule + ".getActivity(), \"" + activitys.get(i) +"\");");
      writer.println("}\n");
      writer.flush();
    }
    writer.println("}");
    writer.close();
  }

  @Override
  public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
          throws InterruptedException, IOException {

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
      docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.parse("D:\\Temp\\AndroidManifest.xml");

      paserXML(doc);
      String packageName = getPackageName(doc);
      ArrayList<String> activitys = paserXML(doc);
      generateTestFile(packageName, activitys);

    } catch (ParserConfigurationException | SAXException e) {
      e.printStackTrace();
    }
  }

  @Symbol("greet")
  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Selenium Screenshot Method";
    }

  }

}
