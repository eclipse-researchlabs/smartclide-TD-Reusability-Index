/*******************************************************************************
 * Copyright (C) 2021-2022 UoM - University of Macedonia
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package gr.zisis.reusabilityapi.service;

import gr.zisis.reusabilityapi.controller.response.entity.*;
import gr.zisis.reusabilityapi.domain.ReusabilityMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

import static java.lang.Math.log10;


@Service
public class ReusabilityServiceBean implements ReusabilityService {

    @Autowired
    private WebClient webClient;

    @Value("${interest-service.url}")
    private String interestServiceURL;

    @Override
    public Collection<FileReusabilityIndex> findReusabilityIndexByCommit(String url, String sha, Integer limit) {
        WebClient.ResponseSpec responseSpec;
        try {
            if (Objects.nonNull(limit)) {
                responseSpec = webClient.get()
                        .uri(interestServiceURL + "/api/reusabilityMetricsByCommit?url=" + url + "&sha=" + sha + "&limit=" + limit)
                        .retrieve();
            } else {
                responseSpec = webClient.get()
                        .uri(interestServiceURL + "/api/reusabilityMetricsByCommit?url=" + url + "&sha=" + sha)
                        .retrieve();
            }
            List<ReusabilityMetrics> metrics = Arrays.asList(Objects.requireNonNull(responseSpec.bodyToMono(ReusabilityMetrics[].class).block()));
            if (metrics.isEmpty())
                return new ArrayList<>();
            List<FileReusabilityIndex> fileReusabilityIndexList = new ArrayList<>();
            for (ReusabilityMetrics m : metrics) {
                double index = -1 * (8.753 * log10(m.getCbo().doubleValue() + 1) + 2.505 * log10(m.getDit() + 1) - 1.922 * log10(m.getWmc().doubleValue() + 1) + 0.892 * log10(m.getRfc().doubleValue() + 1) - 0.399 * log10(m.getLcom().doubleValue() + 1) - 1.080 * log10(m.getNocc() + 1));
                fileReusabilityIndexList.add(new FileReusabilityIndex(m.getSha(), m.getRevisionCount(), m.getFilePath(), index));
            }
            Collections.sort(fileReusabilityIndexList);
            return fileReusabilityIndexList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<FileReusabilityIndex> findReusabilityIndexByCommitAndFile(String url, String sha, String filePath) {
        WebClient.ResponseSpec responseSpec;
        try {
            responseSpec = webClient.get()
                    .uri( interestServiceURL + "/api/reusabilityMetricsByCommitAndFile?url=" + url + "&sha=" + sha + "&filePath=" + filePath)
                    .retrieve();
            List<ReusabilityMetrics> metrics = Arrays.asList(Objects.requireNonNull(responseSpec.bodyToMono(ReusabilityMetrics[].class).block()));
            if (metrics.isEmpty())
                return new ArrayList<>();
            List<FileReusabilityIndex> fileReusabilityIndexList = new ArrayList<>();
            for (ReusabilityMetrics m : metrics) {
                double index = -1 * (8.753 * log10(m.getCbo().doubleValue() + 1) + 2.505 * log10(m.getDit() + 1) - 1.922 * log10(m.getWmc().doubleValue() + 1) + 0.892 * log10(m.getRfc().doubleValue() + 1) - 0.399 * log10(m.getLcom().doubleValue() + 1) - 1.080 * log10(m.getNocc() + 1));
                fileReusabilityIndexList.add(new FileReusabilityIndex(m.getSha(), m.getRevisionCount(), filePath, index));
            }
            return fileReusabilityIndexList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<ProjectReusabilityIndex> findProjectReusabilityIndexByCommit(String url, String sha) {
        WebClient.ResponseSpec responseSpec;
        try {
            responseSpec = webClient.get()
                    .uri(interestServiceURL + "/api/reusabilityMetricsByCommit?url=" + url + "&sha=" + sha)
                    .retrieve();
            List<ReusabilityMetrics> metrics = Arrays.asList(Objects.requireNonNull(responseSpec.bodyToMono(ReusabilityMetrics[].class).block()));
            if (metrics.isEmpty())
                return new ArrayList<>();
            double avgIndex = 0.0;
            for (ReusabilityMetrics m : metrics) {
                double index = -1 * (8.753 * log10(m.getCbo().doubleValue() + 1) + 2.505 * log10(m.getDit() + 1) - 1.922 * log10(m.getWmc().doubleValue() + 1) + 0.892 * log10(m.getRfc().doubleValue() + 1) - 0.399 * log10(m.getLcom().doubleValue() + 1) - 1.080 * log10(m.getNocc() + 1));
                avgIndex += index;
            }
            avgIndex /= metrics.size();
            return Collections.singletonList(new ProjectReusabilityIndex(sha, metrics.get(0).getRevisionCount(), avgIndex));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<ProjectReusabilityIndex> findProjectReusabilityIndexPerCommit(String url, Integer limit) {
        List<ProjectReusabilityIndex> projectReusabilityIndexList = new ArrayList<>();
        WebClient.ResponseSpec responseSpec;
        try {
            if (Objects.isNull(limit))
                responseSpec = webClient.get()
                        .uri(interestServiceURL + "/api/reusabilityMetrics?url=" + url)
                        .retrieve();
            else
                responseSpec = webClient.get()
                        .uri(interestServiceURL + "/api/reusabilityMetrics?url=" + url + "&limit=" + limit)
                        .retrieve();
            List<ReusabilityMetrics> metrics = Arrays.asList(Objects.requireNonNull(responseSpec.bodyToMono(ReusabilityMetrics[].class).block()));
            if (metrics.isEmpty())
                return new ArrayList<>();
            for (ReusabilityMetrics m : metrics) {
                double avgIndex = -1 * (8.753 * log10(m.getCbo().doubleValue() + 1) + 2.505 * log10(m.getDit() + 1) - 1.922 * log10(m.getWmc().doubleValue() + 1) + 0.892 * log10(m.getRfc().doubleValue() + 1) - 0.399 * log10(m.getLcom().doubleValue() + 1) - 1.080 * log10(m.getNocc() + 1));
                projectReusabilityIndexList.add(new ProjectReusabilityIndex(m.getSha(), m.getRevisionCount(), avgIndex));
            }
        } catch(Exception e)

        {
            e.printStackTrace();
        }
        Collections.sort(projectReusabilityIndexList);
        return projectReusabilityIndexList;
    }

}
