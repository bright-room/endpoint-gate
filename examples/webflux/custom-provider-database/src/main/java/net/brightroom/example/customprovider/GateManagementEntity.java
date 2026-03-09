package net.brightroom.example.customprovider;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("gate_management")
public class GateManagementEntity {

  @Id
  @Column("gate_id")
  private String gateId;

  @Column("enabled")
  private boolean enabled;

  public String getGateId() {
    return gateId;
  }

  public void setGateId(String gateId) {
    this.gateId = gateId;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
