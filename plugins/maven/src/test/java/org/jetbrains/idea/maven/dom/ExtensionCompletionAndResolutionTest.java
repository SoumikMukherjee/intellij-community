package org.jetbrains.idea.maven.dom;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.idea.maven.indices.MavenIndicesTestFixture;

public class ExtensionCompletionAndResolutionTest extends MavenCompletionAndResolutionWithIndicesTestCase {
  @Override
  protected MavenIndicesTestFixture createIndicesFixture() {
    return new MavenIndicesTestFixture(myDir, myProject, "plugins");
  }

  @Override
  protected void setUpInWriteAction() throws Exception {
    super.setUpInWriteAction();

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");
  }

  public void testGroupIdCompletion() throws Exception {
    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <extension>" +
                     "      <groupId><caret></groupId>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "test", "org.apache.maven.plugins", "org.codehaus.mojo");
  }

  public void testArtifactIdCompletion() throws Exception {
    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <extension>" +
                     "      <groupId>org.apache.maven.plugins</groupId>" +
                     "      <artifactId><caret></artifactId>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");

    assertCompletionVariants(myProjectPom, "maven-compiler-plugin", "maven-war-plugin", "maven-eclipse-plugin");
  }

  public void testArtifactWithoutGroupCompletion() throws Exception {
    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <extension>" +
                     "      <artifactId><caret></artifactId>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");

    assertCompletionVariants(myProjectPom,
                             "maven-compiler-plugin",
                             "maven-war-plugin",
                             "build-helper-maven-plugin",
                             "maven-eclipse-plugin");
  }

  public void testResolving() throws Exception {
    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <extension>" +
                     "      <artifactId><caret>maven-compiler-plugin</artifactId>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");

    PsiReference ref = getReferenceAtCaret(myProjectPom);
    assertNotNull(ref);

    String pluginPath = "plugins/org/apache/maven/plugins/maven-compiler-plugin/2.0.2/maven-compiler-plugin-2.0.2.pom";
    String filePath = myIndicesFixture.getRepositoryHelper().getTestDataPath(pluginPath);
    VirtualFile f = LocalFileSystem.getInstance().findFileByPath(filePath);
    assertEquals(getPsiFile(f), ref.resolve());
  }

  public void testResolvingAbsentPlugins() throws Exception {
    removeFromLocalRepository("org/apache/maven/plugins/maven-compiler-plugin");

    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <extension>" +
                     "      <artifactId><caret>maven-compiler-plugin</artifactId>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");

    PsiReference ref = getReferenceAtCaret(myProjectPom);
    assertNotNull(ref);
    ref.resolve(); // shouldn't throw;
  }

  public void testDoNotHighlightAbsentGroupIdAndVersion() throws Throwable {
    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <extension>" +
                     "      <artifactId>maven-compiler-plugin</artifactId>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");
    checkHighlighting();
  }

  public void testHighlightingAbsentArtifactId() throws Throwable {
    updateProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<build>" +
                     "  <extensions>" +
                     "    <<error descr=\"'artifactId' child tag should be defined\">extension</error>>" +
                     "    </extension>" +
                     "  </extensions>" +
                     "</build>");

    checkHighlighting();
  }
}