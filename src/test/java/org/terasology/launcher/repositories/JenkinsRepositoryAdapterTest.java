package org.terasology.launcher.repositories;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameRelease;
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

  @Test
  void fetchReleases_responseWithoutBuilds() throws IOException {
    BufferedReader stubReader = Mockito.mock(BufferedReader.class);
    when(stubReader.readLine()).thenReturn("");

    Gson stubGson = Mockito.mock(Gson.class);
    when(stubGson.fromJson(stubReader, Jenkins.ApiResult.class)).thenReturn(new Jenkins.ApiResult());

    final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, stubGson);
    final JenkinsRepositoryAdapter stub = Mockito.spy(adapter);
    Mockito.when(stub.openConnection()).thenReturn(stubReader);

    Assertions.assertTrue(adapter.fetchReleases().isEmpty());  
  }

  @Test
  void fetchReleases_assumeInvalidRelease() throws IOException {
    Jenkins.Build buildStub = new Jenkins.Build();
    Jenkins.ApiResult resultStub = new Jenkins.ApiResult();
    resultStub.builds = new Jenkins.Build[]{buildStub};

    Gson gsonStub = Mockito.mock(Gson.class);
    Mockito.doReturn(resultStub).when(gsonStub).fromJson(Mockito.any(BufferedReader.class), Mockito.eq(Jenkins.ApiResult.class));

    final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, gsonStub);
    final JenkinsRepositoryAdapter spy = Mockito.spy(adapter);

    Mockito.doReturn(Optional.empty()).when(spy).computeReleaseFrom(buildStub);

    Assertions.assertTrue(spy.fetchReleases().isEmpty());
    // behavior to ensure that we are testing the correct code path
    Mockito.verify(spy).computeReleaseFrom(buildStub);
  }

  @Test
  void fetchReleases_assumeValidRelease() throws IOException {
    final GameRelease expected = new GameRelease(null, null, null, null);

    Jenkins.Build buildStub = new Jenkins.Build();
    Jenkins.ApiResult resultStub = new Jenkins.ApiResult();
    resultStub.builds = new Jenkins.Build[]{buildStub};

    Gson gsonStub = Mockito.mock(Gson.class);
    Mockito.doReturn(resultStub).when(gsonStub).fromJson(Mockito.any(BufferedReader.class), Mockito.eq(Jenkins.ApiResult.class));

    final JenkinsRepositoryAdapter adapter = new JenkinsRepositoryAdapter(Profile.OMEGA, Build.STABLE, gsonStub);
    final JenkinsRepositoryAdapter spy = Mockito.spy(adapter);

    Mockito.doReturn(Optional.of(expected)).when(spy).computeReleaseFrom(buildStub);

    Assertions.assertIterableEquals(Lists.newArrayList(expected), spy.fetchReleases());
  }
}
