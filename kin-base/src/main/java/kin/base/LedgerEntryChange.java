package kin.base;


import kin.base.xdr.LedgerEntry;


public class LedgerEntryChange {
  LedgerEntryChange() {
  }

  private Long lastModifiedLedgerSequence;

  public Long getLastModifiedLedgerSequence() {
    return this.lastModifiedLedgerSequence;
  }

  public static LedgerEntryChange fromXdr(LedgerEntry xdr) {
    LedgerEntryChange entryChange = null;
    switch (xdr.getData().getDiscriminant()) {
      case ACCOUNT:
        entryChange = AccountLedgerEntryChange.fromXdr(xdr.getData().getAccount());
        entryChange.lastModifiedLedgerSequence = xdr.getLastModifiedLedgerSeq().getUint32().longValue();
        break;
      case TRUSTLINE:
        entryChange = TrustLineLedgerEntryChange.fromXdr(xdr.getData().getTrustLine());
        entryChange.lastModifiedLedgerSequence = xdr.getLastModifiedLedgerSeq().getUint32().longValue();
        break;
      case OFFER:
        break;
      case DATA:
        break;
    }
    return entryChange;
  }
}
