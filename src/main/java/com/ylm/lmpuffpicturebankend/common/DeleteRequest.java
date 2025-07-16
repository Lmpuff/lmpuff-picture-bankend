package com.ylm.lmpuffpicturebankend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -3572808757294263473L;

    /**
     * id
     */
    private Long id;
}
