package org.terasology.launcher.repositories;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.gson.Gson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.Profile;

public class JenkinsRepositoryAdapterTest {

  @Test
  void fetchReleases_ioExceptionOnApi() throws IOException {
    final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, new Gson());
    final JenkinsRepositoryAdapter stub = Mockito.spy(adapter);
    Mockito.when(stub.openConnection()).thenThrow(IOException.class);

    Assertions.assertTrue(adapter.fetchReleases().isEmpty());  
  }

  @Test
  void fetchReleases_emptyResponse() throws IOException {
    BufferedReader stubReader = Mockito.mock(BufferedReader.class);
    when(stubReader.readLine()).thenReturn("");

    final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, new Gson());
    final JenkinsRepositoryAdapter stub = Mockito.spy(adapter);
    Mockito.when(stub.openConnection()).thenReturn(stubReader);

    Assertions.assertTrue(adapter.fetchReleases().isEmpty());  
  }
}
