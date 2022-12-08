package org.example.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
    private Long Id;
    private String customerName;
    private String rescode;
    private String data;
    private Integer amount;
    private Integer debitAmount;
    private Integer realAmount;
    private Integer payDate;
    private Date sysdate;
}
