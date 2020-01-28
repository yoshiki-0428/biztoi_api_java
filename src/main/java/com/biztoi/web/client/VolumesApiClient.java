package com.biztoi.web.client;

import com.biztoi.api.VolumesApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "googleBooksApi", url= "https://www.googleapis.com/books/v1", configuration = com.biztoi.web.config.FeignConfiguration.class)
public interface VolumesApiClient extends VolumesApi {}
