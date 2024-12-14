package jp.synthtarou.mixtone.synth.soundfont.struct;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import jp.synthtarou.mixtone.synth.soundfont.XTFile;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTStructForHeader {
    public XTStructForHeader(XTFile sfz){
        _sfz = sfz;

        SFZElement ifil = _sfz.getElement("ifil");
        if (ifil != null) {
            XTRow row = ifil.get(0);
            _infoFileMajor = row.intColumn(SFZElement.IFIL_MAJOR);
            _infoFileMinor = row.intColumn(SFZElement.IFIL_MINOR);
        }
        else {
            _infoFileMajor = 0;
            _infoFileMinor = 0;
        }
        SFZElement isng = _sfz.getElement("isng");
        if (isng != null) {
            XTRow row = isng.get(0);
            _infoSoundGear = row.textColumn(SFZElement.ISNG_NAME);
        }
        else {
            _infoSoundGear = null;
        }
        SFZElement INAM = _sfz.getElement("INAM");
        if (INAM != null) {
            XTRow row = INAM.get(0);
            _infoName = row.textColumn(SFZElement.INAME_NAME);
        }
        else {
            _infoName = null;
        }
        SFZElement irom = _sfz.getElement("irom");
        if (irom != null) {
            XTRow row = irom.get(0);
            _infoHardRomName = row.textColumn(SFZElement.IROM_NAME);
        }
        else {
            _infoHardRomName = null;
        }
        SFZElement iver = _sfz.getElement("iver");
        if (iver != null) {
            XTRow row = iver.get(0);
            _infoRomSoftVerMajor = row.intColumn(SFZElement.IVER_MAJOR);
            _infoRomSoftVerMinor = row.intColumn(SFZElement.IVER_MINOR);
        }
        else {
            _infoRomSoftVerMajor = 0;
            _infoRomSoftVerMinor = 0;
        }
        SFZElement ICRD = _sfz.getElement("ICRD");
        if (ICRD != null) {
            XTRow row = ICRD.get(0);
            _infoCreatedDate = row.textColumn(SFZElement.ICRD_DATE);
        }
        else {
            _infoCreatedDate = null;
        }
        SFZElement IENG = _sfz.getElement("IENG");
        if (IENG != null) {
            XTRow row = IENG.get(0);
            _infoEngeneer = row.textColumn(SFZElement.IENG_NAME);
        }
        else {
            _infoEngeneer = null;
        }
        SFZElement IPRD = _sfz.getElement("IPRD");
        if (IPRD != null) {
            XTRow row = IPRD.get(0);
            _infoProduct = row.textColumn(SFZElement.IPRD_NAME);
        }
        else {
            _infoProduct = null;
        }
        SFZElement ICOP = _sfz.getElement("ICOP");
        if (ICOP != null) {
            XTRow row = ICOP.get(0);
            _infoCopyright = row.textColumn(SFZElement.ICOP_NAME);
        }
        else {
            _infoCopyright = null;
        }
        SFZElement ICMT = _sfz.getElement("ICMT");
        if (ICMT != null) {
            XTRow row = ICMT.get(0);
            _infoCommnet = row.textColumn(SFZElement.ICMT_TEXT);
        }
        else {
            _infoCommnet = null;
        }
        SFZElement ISFT = _sfz.getElement("ISFT");
        if (ISFT != null) {
            XTRow row = ISFT.get(0);
            _infoSoftware = row.textColumn(SFZElement.ISFT_NAME);
        }
        else {
            _infoSoftware = null;
        }
    }

    final XTFile _sfz;
    final int _infoFileMajor;
    final int _infoFileMinor;
    final String _infoSoundGear;
    final String _infoName;
    final String _infoHardRomName;
    final int _infoRomSoftVerMajor;
    final int _infoRomSoftVerMinor;
    final String _infoCreatedDate;
    final String _infoEngeneer;
    final String _infoProduct;
    final String _infoCopyright;
    final String _infoCommnet;
    final String _infoSoftware;
}
