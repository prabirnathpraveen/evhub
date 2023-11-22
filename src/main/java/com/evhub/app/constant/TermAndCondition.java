package com.evhub.app.constant;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TermAndCondition {
    public Map<String, String> termsAndCondition1() {
        Map<String, String> terAndCondition = new LinkedHashMap<>();
        terAndCondition.put("A.The report","\nThe report contains EV/Hybrid vehicle identification,as well as a              description of the scope of the test with measurement results where          available.");
        terAndCondition.put("B. Assessment", "\nThe points on which the car is tested appear in the test. The points are " +
                "dynamic and adapted to the EV/Hybrid car's engine and drivetrain." +
                " Description appear under Description with an explanation of the error" +
                " or the extent of the deficiency with photo documentation where " +
                "possible. The pricing is based on what our workshop partners can offer and the rating of the price is based on the evaluation EV HUB see towards the condition of the EV/Hybrid cars value adjusted for the average in the car industry. Prices may differ from or workshop partners.");
        terAndCondition.put("C. Ownership", "\nThe test result belongs to our partners workshop customer. The test results " +
                "are not handed out to others without the consent of our partners workshop " +
                "customer. ");
        terAndCondition.put("", "");
        terAndCondition.put("D. Dismantling", "\nEV HUB will assist our workshop partners to disassemble for inspection/" +
                "assessment, and point out faults that are found during disassembly.");

        return terAndCondition;
    }

    public Map<String, String> termsAndCondition2() {
        Map<String, String> termAndCondition = new LinkedHashMap<>();
        termAndCondition.put("E. Test driving, periodic and future problems",
                "\nPeriodic errors and future problems can only be used as a guideline to potential errors and can be deviated from what's that are discovered at the time of the test. The test is to be considered a \"snapshot\" of the car. Periodic errors/deviations/oil consumption can occur without this being detectable due to absence of symptoms (appearance) at the time of the test. It's very hard to predict future problems, errors or deviations. The test is not to be regarded as a " +
                        "periodic control, and there may be deviations in assessment where there is room for discretionary assessment. It is only stated when the deadline for the next inspection as an recommendation, not whether there are deficiencies from a previous periodic inspection. We test drives the car to a limited extent, " +
                        "short trips in the vicinity of the test station and is dependent on driving conditions and the car's technical condition. Technical installations that require " +
                        "special driving conditions to check functions such as 4x4 etc. is only tested if the driving conditions make it possible to test the function at the time of the test..");
        termAndCondition.put("F. Reading of diagnosis","\nEV HUB reads error messages that the car's manufacturer has given access to and which are available at the time of the test.\n"+
                "Diagnostic readings are read with current test equipment used by Viking. The points that appear under Reading out error codes. If there is uncertainty about " +
                "the points, EV HUB recommends a further investigation. EV HUB cannot detect deviations in the car's stated mileage using diagnostics.");
        termAndCondition.put("G . High voltage components","\nOperating batteries on electric and hybrid cars will be throughly checked with our instruments and can document the exact capacity of the battery. EV HUB cannot guarantee that the capacity at the time of the test corresponds to the manufacturer's stated data.");
        termAndCondition.put("EVHUB's responsibility","\nEV HUB has assessed all control points accordingly and at its best judgement. Control points that are not mentioned in the report are not part of the assessment. A ny errors/deviations that are discovered after the inspection can only be charged to EV HUB if this is due to negligence when carrying out the inspection. However, in the case of liability for EV HUB, the compensation sum must never exceed the contract sum. The contract sum here means prices for the individual test. If there are any discrepancies in connection with the delivery, the customer can take this up with our workshop partners. A ny complaints must reach our workshop partners no later than 10 days after the test has been carried out. EV HUB reserves the right to check the point being advertised before further proceedings..");
        return termAndCondition;
    }
}
