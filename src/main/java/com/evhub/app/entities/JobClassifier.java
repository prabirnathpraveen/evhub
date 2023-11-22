package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobClassifier {
    private String status;
    private String result;
    private String description ;
    private String measuredValue;
    private String unit;
    //private String errorAndDeficiency;
    private String estimatedRepairCostStandard;
    private String estimatedRepairCostPlanetFriendly;


    //Winter Tire
    private String Brand;

    private String productionMonthYear;

    private String tireDimension;

    private  String tirePatternFrontLeft;

    private String tirePatternFrontRight;

    private String tirePatternRearLeft;

    private String tirePatternRearRight;

    //Winter Tire ends

    //Brake Discs
    private String brakeDiscThicknessFrontLeft;

    private String brakeDiscThicknessFrontRight;

    private String brakeDiscThicknessRearRight;

    private String brakeDiscThicknessRearLeft;

   //Brake Discs end




    private List<String> image;
    private List<String> file;

}
