package com.github.ar4ik4ik.cloudstorage.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface DownloadStrategy {

    StreamingResponseBody download(String resourcePath);
}
