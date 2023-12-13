package com.andrew.tgbot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestEntity {
    private String query;
    private int count;
}
