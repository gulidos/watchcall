package ru.in.watcher.sip.content;


import java.io.UnsupportedEncodingException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.in.isp.ACM;
import ru.in.isp.ANM;
import ru.in.isp.CON_;
import ru.in.isp.CPG;
import ru.in.isp.CauseCode;
import ru.in.isp.IAM;
import ru.in.isp.Indicators;
import ru.in.isp.Isup;
import ru.in.isp.IsupAddress;
import ru.in.isp.REL;
import ru.in.isp.RLC;
import ru.in.watcher.conf.Settings;

public class IsupUtils {

    private static Logger logger = LoggerFactory.getLogger(IsupUtils.class);

    public static byte[] parseIsup(SipServletMessage sipMsg) {
          try {
            // application/isup;version=itu-t92+;base=itu-t92+
            // application/isup;base=itu-t92+;version=itu-t92+
            String s = Contents.parseContent(sipMsg, Contents.MIME_ISUP);
            if (s != null) {
                
                byte[] buf = s.getBytes(Contents.CONTENT_ENCODING);
                return buf;
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    public static IAM createIAM(IsupAddress addrA, IsupAddress addrB) {
        IAM iam = new IAM(addrA, addrB);
        iam.setCgCat((byte) Settings.ISUP_CGCAT);
        return iam;
    }


    private static REL createREL(CauseCode causeCode) {
        REL rel = new REL();
        rel.setCause(causeCode.code);
        return rel;
    }


    public static void addSipiREL(SipServletMessage sipMsg, CauseCode causeCode) throws UnsupportedEncodingException {
        REL rel = IsupUtils.createREL(causeCode);
        sipMsg.setContent(rel.encode(), Isup.SIP_I_Content_Type);
        sipMsg.setHeader("Content-Type", Isup.SIP_I_Content_Type);
        sipMsg.setHeader("Content-Disposition", Isup.SIP_I_Content_Disposition);
    }


    public static RLC addSipiRLC(SipServletMessage sipMsg) throws UnsupportedEncodingException {
        RLC rlc = new RLC();
        sipMsg.setContent(rlc.encode(), Isup.SIP_I_Content_Type);
        sipMsg.setHeader("Content-Type", Isup.SIP_I_Content_Type);
        sipMsg.setHeader("Content-Disposition", Isup.SIP_I_Content_Disposition);
        return rlc;
    }

    
	public static int getCause(Isup isup) {
		try {
			byte[] cai = isup.getIndSafe(Indicators.CAI); //Cause indicator
			if (cai == null) {
				return 0;
			}
			return (int) (cai[1] & 0x7f);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0;
	}
	
	
	public static String getRNN(Isup isup) {
		if (isup == null) return null;
		
        byte[] rnn = isup.getIndSafe(Indicators.RNN);
        if (rnn == null) return null;
        
		try {
			IsupAddress addr = new IsupAddress(2, rnn);
			logger.debug("[RNN] {}", addr.num);

			 return  addr.num;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
    
    
    public static Isup getIsup(SipServletResponse resp)  {
    	byte[] isupBuf = parseIsup(resp);
        int typ = Isup.getType(isupBuf);
        try {
			switch (typ) {
			    case Isup.ACM: return new ACM(isupBuf);
			    case Isup.ANM: return new ANM();
			    case Isup.CON: return new CON_();
			    case Isup.IAM: return new IAM(isupBuf);
			    case Isup.REL: return new REL(isupBuf);
			    case Isup.RLC: return new RLC();
			    case Isup.CPG: return new CPG(isupBuf);
			    default: return null;
			}
		} catch (Exception e) {
			  logger.error(e.getMessage(), e);
		}
        return null;
    }
}
