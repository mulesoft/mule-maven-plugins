/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateTestSourcesMojoTest extends AbstractMuleMojoTest {
    private static final String EXPECTED_EXCEPTION_MESSAGE_FAIL_GENERATE_SOURCES = "Fail to generate sources";
    private GenerateTestSourcesMojo mojo;

    @Before
    public void before() throws IOException {
        buildTemporaryFolder.create();
        projectMock = mock(MavenProject.class);
        buildMock = mock(Build.class);
        mojo = new GenerateTestSourcesMojo();
        testMuleFolder = buildTemporaryFolder.newFolder(TEST_MULE);
        munitFolder = new File(testMuleFolder.getAbsolutePath(), MUNIT);
        munitFolder.mkdir();
        munitSourceFolder = temporaryFolder.getRoot();
        mojo.munitSourceFolder = munitSourceFolder;
        mojo.project = projectMock;
    }
    @Test
    public void createTestMuleFolderContentWithoutTestThrowsExceptionTest() throws IOException, MojoFailureException, MojoExecutionException {
        expectedEx.expect(MojoFailureException.class);
        expectedEx.expectMessage(EXPECTED_EXCEPTION_MESSAGE_FAIL_GENERATE_SOURCES);
        when(buildMock.getDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        mojo.execute();
    }

    @Test
    public void createTestMuleFolderContentTest() throws IOException, MojoFailureException, MojoExecutionException {
        when(buildMock.getDirectory()).thenReturn(buildTemporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);

        File munitTestFile = temporaryFolder.newFile(MUNIT_TEST_FILE_NAME);
        munitFolder.createNewFile();

        mojo.execute();

        File[] filesInMunitFolder = munitFolder.listFiles();
        assertThat("The munit folder should contain one file", filesInMunitFolder.length == 1 && filesInMunitFolder[0].isFile());
        assertThat("The file in the munit folder is not the expected", filesInMunitFolder[0].getName(), equalTo(munitTestFile.getName()));
    }
}
