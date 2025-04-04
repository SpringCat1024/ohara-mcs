package org.ohara.msc.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ohara.msc.listener.ConfigData;

import java.lang.annotation.Annotation;

/**
 * @date 2025-03-26 16:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerAddress implements ConfigData {
    private String host;
    private Integer port;
    private boolean active = true;
}
