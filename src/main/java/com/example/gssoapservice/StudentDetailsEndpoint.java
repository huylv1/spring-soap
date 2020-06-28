package com.example.gssoapservice;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;


@Endpoint
public class StudentDetailsEndpoint {
    private static final String NAMESPACE_URI = "http://example.com/gssoapservice";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetStudentDetailsRequest")
    @ResponsePayload
    public GetStudentDetailsResponse processCourseDetailsRequest(@RequestPayload GetStudentDetailsRequest request) {
        GetStudentDetailsResponse response = new GetStudentDetailsResponse();

        StudentDetails studentDetails = new StudentDetails();
        studentDetails.setId(request.getId());
        studentDetails.setName("Adam");
        studentDetails.setPassportNumber("E1234567");

        response.setStudentDetails(studentDetails);

        return response;
    }


}