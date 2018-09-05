package com.pigeon.post.messaging.messagingdemo.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column
    private Integer id;

    @Column
    private String authId;

    @Column
    private String username;

    @OneToMany(mappedBy = "account")
    private List<Contact> contacts;
}
