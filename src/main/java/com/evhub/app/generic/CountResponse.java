package com.evhub.app.generic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class CountResponse<T> {

   private Long count;
   private List<T> response=new ArrayList<>();

}
