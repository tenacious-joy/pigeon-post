package com.pigeon.post.messaging.messagingdemo.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "phone_number")
public class Contact {

    @Column
    private String number;

    @Id
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;
}
