package cn.dong.nexus.common.domain.bo;

import lombok.AllArgsConstructor;import lombok.Data;import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentOwnerSaveBO {

    private String id;

    private String ownerType;

    private String ownerId;
}
