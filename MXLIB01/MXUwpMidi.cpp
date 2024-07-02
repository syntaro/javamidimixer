#include "pch.h"

#include "MXLIB01UWPMidi.h"

extern JavaVM* _javaVM;

using namespace std;
using namespace winrt;
using namespace Windows::Foundation;
using namespace Windows::Security::Credentials::UI;
using namespace Windows::ApplicationModel::DataTransfer; // 追加
using namespace Windows::Foundation; // 追加
using namespace Windows::Devices::Midi; // 追加
using namespace Windows::Devices::Enumeration; // 追加
using namespace Windows::Storage::Streams;

enum class PortType {
    INPUT,
    OUTPUT,
};

struct PortInformation {
public:
    wchar_t* name;
    wchar_t* id;
    int number;

    PortType whichType;
    IMidiInPort itsInput;
    IMidiOutPort itsOutput;
    bool nowExist;
};

wchar_t* wstrdup(const wchar_t* in) {
    size_t len = wcslen(in) + 1;
    wchar_t* result = (wchar_t*)malloc(sizeof(wchar_t) * len);
    if (result != nullptr) {
        memcpy(result, in, sizeof(wchar_t) * len);
    }
    return result;
};

extern void receiver(MidiInPort port, IMidiMessageReceivedEventArgs args);

vector<PortInformation*> inputArray;
vector<PortInformation*> outputArray;

bool openerOpen(PortInformation* info, int timeout) {
    if (info->whichType == PortType::INPUT) {
        IAsyncOperation<MidiInPort> ope = MidiInPort::FromIdAsync(info->id);
        time_t started = time(nullptr);

        while (true) {
            auto x = ope.Status();
            if (x == Windows::Foundation::AsyncStatus::Completed) {
                break;
            }
            if (x == Windows::Foundation::AsyncStatus::Canceled) {
                break;
            }
            if (x == Windows::Foundation::AsyncStatus::Error) {
                break;
            }
            time_t spend = time(nullptr) - started;
            std::wcout << L"timeout= " << std::to_wstring(timeout) << L" spend = " << std::to_wstring(spend) << std::endl;
            if (timeout != 0) {
                if (spend * 1000 >= timeout) {
                    break;
                }
            }

            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }

        info->itsInput = nullptr;
        if (ope.Status() == Windows::Foundation::AsyncStatus::Completed) {
            std::wcout << L"open success " << std::endl;
            ope.GetResults().MessageReceived(receiver);
            info->itsInput = ope.GetResults();
            return true;
        }
        else {
            ope.Cancel();
            return false;
        }
    }
    else {
        IAsyncOperation<IMidiOutPort> ope = MidiOutPort::FromIdAsync(info->id);
        time_t x = time(nullptr);

        while (ope.Status() == Windows::Foundation::AsyncStatus::Started) {
            if (timeout != 0) {
                if (time(nullptr) >= x + timeout) {
                    break;
                }
            }

            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }

        info->itsOutput = nullptr;
        if (ope.Status() == Windows::Foundation::AsyncStatus::Completed) {
            info->itsOutput = ope.GetResults();
            return true;
        }
        else {
            if (ope.Status() != Windows::Foundation::AsyncStatus::Canceled) {
                ope.Cancel();
            }
            return true;
        }
    }
    return false;
}

PortInformation* Input(int device) {
    if (device < 0 || device > inputArray.size()) {
        return nullptr;
    }
    return inputArray[device];
}

PortInformation* Output(int device) {
    if (device < 0 || device > outputArray.size()) {
        return nullptr;
    }
    return outputArray[device];
}

void MXDeviceManager::InitObject() {
    if (initFlag) {
        return;
    }
    initFlag = true;

    debugText(L"in reload");
    InReload();
    debugText(L"out reload");
    OutReload();

    debugText(L"out reload2");
    /* 時間かかって意味がなかった

    wstring accept = L"System.Devices.DevObjectType:=5 AND System.Devices.Aep.ProtocolId:=\"{BB7BB05E-5972-42B5-94FC-76EAA7084D49}\" AND ((System.Devices.Aep.IsPaired:=System.StructuredQueryType.Boolean#True))";
    wstring ignore = L"System.Devices.DevObjectType:=5 AND System.Devices.Aep.ProtocolId:=\"{BB7BB05E-5972-42B5-94FC-76EAA7084D49}\" AND ((System.Devices.Aep.IsPaired:=System.StructuredQueryType.Boolean#False) OR System.Devices.Aep.Bluetooth.IssueInquiry:=System.StructuredQueryType.Boolean#True)";

    debugText(L"Enum Bluetooth Accept =  ");
    debugText(accept);

    IAsyncOperation<DeviceInformationCollection> listAccept = DeviceInformation::FindAllAsync(accept);
    while (listAccept.Status() == Windows::Foundation::AsyncStatus::Started)
    {
        std::this_thread::sleep_for(std::chrono::microseconds(100));
    }
    if (listAccept.Status() == Windows::Foundation::AsyncStatus::Completed) {
        DeviceInformationCollection col = listAccept.GetResults();
        debugText(L"Count = ");
        debugText(to_wstring(col.Size()));
        //新規だけ追加する
        for (unsigned int i = 0; i < col.Size(); ++i)
        {
            wstring str;
            str += col.GetAt(i).Id();
            str += L" == ";
            str += col.GetAt(i).Name();
            debugText(str);
        }
    }

    debugText(L"Enum Bluetooth Ignore =  ");
    debugText(ignore);

    IAsyncOperation<DeviceInformationCollection> listIgnore = DeviceInformation::FindAllAsync(ignore);
    while (listIgnore.Status() == Windows::Foundation::AsyncStatus::Started)
    {
        std::this_thread::sleep_for(std::chrono::microseconds(100));
    }

    boolean changed = false;
    if (listAccept.Status() == Windows::Foundation::AsyncStatus::Completed) {
        DeviceInformationCollection col = listAccept.GetResults();
        debugText(L"Count = ");
        debugText(to_wstring(col.Size()));
        //新規だけ追加する
        for (unsigned int i = 0; i < col.Size(); ++i)
        {
            wstring str;
            str += col.GetAt(i).Id();
            str += L" == ";
            str += col.GetAt(i).Name();
            debugText(str);
        }
    }
    debugText(L"Enum Bluetooth Done");
    */
}

void MXDeviceManager::InReload() {
    // まず、INPUTを列挙
    debugText(L"in reload 1");
    hstring sel = MidiInPort::GetDeviceSelector();
    IAsyncOperation<DeviceInformationCollection> list = DeviceInformation::FindAllAsync(sel);
    while (list.Status() == Windows::Foundation::AsyncStatus::Started)
    {
        std::this_thread::sleep_for(std::chrono::microseconds(100));
    }
    debugText(L"in reload 2");

    boolean changed = false;

    debugText(L"in reload 3");
    if (list.Status() == Windows::Foundation::AsyncStatus::Completed) {
        debugText(L"in reload 3.5");
        DeviceInformationCollection col = list.GetResults();
        debugText(L"in reload 4");

        //std::lock_guard autoLock(staticManager->_mutex);
        debugText(L"in reload 4.5");

        //既存リストのフラグをリセット
        for (PortInformation* already : inputArray) {
            already->nowExist = FALSE;
        }
        debugText(L"in reload 5");
        //新規だけ追加する
        for (unsigned int i = 0; i < col.Size(); ++i)
        {
            PortInformation* info = new PortInformation();
            debugText2(L"Found", col.GetAt(i).Name().c_str());
            info->name = wstrdup(col.GetAt(i).Name().c_str());
            info->id = wstrdup(col.GetAt(i).Id().c_str());
            info->whichType = PortType::INPUT;
            info->itsInput = nullptr;
            info->itsOutput = nullptr;
            info->nowExist = true;
            bool foundPrev = false;

            debugText(L"in reload 6");
            for (PortInformation* already : inputArray) {
                if (wcscmp(already->id, info->id) == 0) {
                    already->nowExist = TRUE;
                    foundPrev = TRUE;
                    break;
                }
            }
            if (foundPrev) {
                delete info;
                continue;
            }
            info->number = (int)inputArray.size();
            inputArray.push_back(info);
            changed = true;
        }
        //削除されたものを閉じる
        debugText(L"in reload 7");
        for (PortInformation* toremove : inputArray) {
            if (toremove->nowExist == FALSE) {
                staticManager->InClose(toremove->number);
                changed = true;
            }
        }
        debugText(L"in reload 8");
        if (changed) {
            refCallDeviceListed();
        }
        debugText(L"in reload 9");
    }
}

void MXDeviceManager::OutReload() {
    //std::lock_guard autoLock(mx_valuelock);
    debugText(L"out reload 1");
    // まず、OUTPUTを列挙
    hstring sel = MidiOutPort::GetDeviceSelector();
    IAsyncOperation<DeviceInformationCollection> list = DeviceInformation::FindAllAsync(sel);
    debugText(L"out reload 2");

    DeviceInformationCollection col = list.get();
    boolean changed = false;

    debugText(L"out reload 3");

    //既存リストのフラグをリセット
    for (PortInformation* already : outputArray) {
        already->nowExist = FALSE;
    }
    //新規だけ追加する
    for (unsigned int i = 0; i < col.Size(); ++i)
    {
        PortInformation* info = new PortInformation();
        info->name = wstrdup(col.GetAt(i).Name().c_str());
        info->id = wstrdup(col.GetAt(i).Id().c_str());
        info->whichType = PortType::OUTPUT;
        info->itsInput = nullptr;
        info->itsOutput = nullptr;
        info->nowExist = TRUE;
        bool foundPrev = FALSE;

        for (PortInformation* already : outputArray) {
            if (wcscmp(already->id, info->id) == 0) {
                already->nowExist = TRUE;
                foundPrev = TRUE;
                break;
            }
        }
        if (foundPrev) {
            delete info;
            continue;
        }
        info->number = outputArray.size();
        outputArray.push_back(info);
        changed = true;
    }

    debugText(L"out reload 4");
    //削除されたものを閉じる
    for (PortInformation* toremove : outputArray) {
        if (toremove->nowExist == FALSE) {
            OutClose(toremove->number);
            changed = true;
        }
    }
    debugText(L"out reload 5");
    if (changed) {
        refCallDeviceListed();
    }
    debugText(L"out reload 6");
}

size_t MXDeviceManager::InRoomSize() {
    //std::lock_guard autoLock(staticManager->_mutex);
    if (!initFlag) {
        debugText(L"initObject ? ");
        InitObject();
    }
    debugText(L"arraySize ? ");
    return inputArray.size();
}

const wchar_t* MXDeviceManager::InName(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Input(device);
    if (info == nullptr) {
        return nullptr;
    }
    return info->name;
}

const wchar_t* MXDeviceManager::InId(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Input(device);
    if (info == nullptr) {
        return nullptr;
    }
    return info->id;
}

bool MXDeviceManager::InIsOpen(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Input(device);
    if (info == nullptr) {
        return false;
    }
    return info->itsInput != nullptr;
}

bool MXDeviceManager::InOpen(int device, long timeout) {
    PortInformation* info = Input(device);
    if (info == nullptr) {
        return false;
    }
    return openerOpen(info, timeout);
}

void MXDeviceManager::InClose(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Input(device);
    if (info == nullptr) {
        return;
    }

    if (info->itsInput != nullptr) {
        debugText(L"InputClosed");
        info->itsInput.Close();
        info->itsInput = nullptr;
    }
}

size_t MXDeviceManager::OutRoomSize() {
    //std::lock_guard autoLock(staticManager->_mutex);
    if (!initFlag) {
        InitObject();
    }
    return outputArray.size();
}

const wchar_t* MXDeviceManager::OutName(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return nullptr;
    }
    return info->name;
}
const wchar_t* MXDeviceManager::OutId(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return nullptr;
    }
    return info->id;
}

bool MXDeviceManager::OutIsOpen(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return false;
    }
    return info->itsOutput != nullptr;
}

bool MXDeviceManager::OutOpen(int device, long timeout) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return false;
    }
    return openerOpen(info, timeout);
}

void MXDeviceManager::OutClose(int device) {
    //std::lock_guard autoLock(staticManager->_mutex);
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return;
    }
    if (info->itsOutput != nullptr) {
        info->itsOutput.Close();
        info->itsOutput = nullptr;
    }
}

/*
MidiMessageType midiDataType(int status, int data1) {
    int command = status & 0xf0;
    int channel = status & 0x0f;

    switch (command) {
    case 0x80:
        return MidiMessageType::NoteOff;
    case 0x90:
        return MidiMessageType::NoteOn;
    case 0xa0:
        return MidiMessageType::PolyphonicKeyPressure;
    case 0xb0:
        return MidiMessageType::ControlChange;
    case 0xc0:
        return MidiMessageType::ProgramChange;
    case 0xd0:
        return MidiMessageType::ChannelPressure;
    case 0xe0:
        return MidiMessageType::PitchBendChange;
    case 0xf0: {
        switch (status) {
        case 0xf0:
            return MidiMessageType::SystemExclusive;
        case 0xf2:
            return MidiMessageType::SongPositionPointer;
        case 0xf3:
            return MidiMessageType::SongSelect;
        case 0xf4://none
            return MidiMessageType::None;
        case 0xf5://none
            return MidiMessageType::None;
        case 0xf6:
            return MidiMessageType::TuneRequest;
        case 0xf7:
            return MidiMessageType::None; //MidiMessageType::SystemExclusive;
        case 0xf8:
            return MidiMessageType::MidiTimeCode;
        case 0xf9://none
            return MidiMessageType::None;
        case 0xfa:
            return MidiMessageType::Start;
        case 0xfb:
            return MidiMessageType::Continue;
        case 0xfc:
            return MidiMessageType::Stop;
        case 0xfd: //none
            return MidiMessageType::None;
        case 0xfe:
            return MidiMessageType::ActiveSensing;
        case 0xff: //system reset
            return MidiMessageType::SystemReset;
        }
    }
    }
    return MidiMessageType::NoteOn;
}
*/

bool MXDeviceManager::OutShortMessage(JNIEnv* env, int device, int message) {
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return false;
    }
    if (info->itsOutput == nullptr) {
        return false;
    }
    int status = (message >> 16) & 0xff;
    int data1 = (message >> 8) & 0xff;
    int data2 = (message) & 0xff;

    int command = status & 0xf0;
    int channel = status & 0x0f;

    if (command == 0x90 && data2 == 0) {
        command = 0x80;
    }


    switch (command) {
    case 0x80: {
        MidiNoteOffMessage msg(channel, data1, data2);
        info->itsOutput.SendMessageW(msg);
    }
             break;
    case 0x90: {
        MidiNoteOnMessage msg(channel, data1, data2);
        info->itsOutput.SendMessageW(msg);
    }
             break;
    case  0xa0: {
        MidiPolyphonicKeyPressureMessage  msg(channel, data1, data2);
        info->itsOutput.SendMessageW(msg);
    }
              break;
    case 0xb0: {
        MidiControlChangeMessage msg(channel, data1, data2);
        info->itsOutput.SendMessageW(msg);
    }
             break;
    case 0xc0: {
        MidiProgramChangeMessage msg(channel, data1);
        info->itsOutput.SendMessageW(msg);

    }
             break;
    case 0xd0: {
        MidiChannelPressureMessage  msg(channel, data1);
        info->itsOutput.SendMessageW(msg);
    }
    break;
    case 0xe0: {
        MidiPitchBendChangeMessage  msg(channel, data2 << 7 | data1);
        info->itsOutput.SendMessageW(msg);
    }
             break;
    case 0xf0:
        switch (status) {
        case 0xf0:
            break;
        case 0xf2: {
            MidiSongPositionPointerMessage  msg(data2 << 7 | data1);
            info->itsOutput.SendMessageW(msg);
            break;
        }
        case 0xf3: {
            MidiSongSelectMessage  msg(data1);
            info->itsOutput.SendMessageW(msg);
            break;
        }
        case 0xf4://none
            break;
        case 0xf5://none
            break;
        case 0xf6: {
            MidiTuneRequestMessage msg;
            info->itsOutput.SendMessageW(msg);
            break;
        }
        case 0xf7:
            break;
            //return MidiMessageType::None; //MidiMessageType::SystemExclusive;
        case 0xf8:  {
            MidiTimingClockMessage msg;
            info->itsOutput.SendMessageW(msg);
            break;
        }
        case 0xf9://none
            return false;
        case 0xfa: {
            MidiStartMessage msg;
            info->itsOutput.SendMessageW(msg);
            return true;
        }
        case 0xfb: {
            MidiContinueMessage msg;
            info->itsOutput.SendMessageW(msg);
            return true;
        }
        case 0xfc: {
            MidiStopMessage msg;
            info->itsOutput.SendMessageW(msg);
            return true;
        }
        case 0xfd: //none
            return false;
        case 0xfe: {
            MidiActiveSensingMessage msg;
            info->itsOutput.SendMessageW(msg);
            return true;
        }
        case 0xff: //system reset
            MidiSystemResetMessage msg;
            info->itsOutput.SendMessageW(msg);
            return true;
        }
        break;
    }

    //TODO UWP Output
    return true;
}

class MyBuffer : public IBuffer
{
public:
    MyBuffer(char* ptr, size_t size) : m_address(ptr), m_size(size), m_count(size) {}
    size_t Capacity() { return m_size; }
    size_t Length() { return m_count; }
    void Length(size_t length) { m_count = length; }
private:
    char* m_address;
    size_t m_size;
    size_t m_count;
};

bool MXDeviceManager::OutLongMessage(JNIEnv* env, int device, jbyteArray data) {
    PortInformation* info = Output(device);
    if (info == nullptr) {
        return false;
    }
    if (info->itsOutput == nullptr) {
        return false;
    }

    _javaVM->AttachCurrentThread((void**)&env, nullptr);
    int len = env->GetArrayLength(data);
    jbyte* pointer = env->GetByteArrayElements(data, nullptr);
    if (len == 0) {
        return true;
    }
    if (len == 3) {
        jbyte b1 = (len >= 1) ? pointer[0] : 0;
        jbyte b2 = (len >= 2) ? pointer[1] : 0;
        jbyte b3 = (len >= 3) ? pointer[2] : 0;
        int x = b1;
        x <<= 8;
        x |= b2;
        x <<= 8;
        x |= b3;
        return OutShortMessage(env, device, x);
    }

    int skip = 0;
    if (pointer[0] == 0xf7) {
    //  cant work both, 0 , 1
        //skip = 1;
    }
    Buffer buf(len - skip);
    char* binary = (char*)buf.data();
    for (int i = 0; i < len - skip; ++i) {
        binary[i] = pointer[i + skip];
    }
    buf.Length(len - skip);

    if (binary[0] == 0xf0 || binary[0] == 0xf7) {
        MidiSystemExclusiveMessage mes(buf);
        info->itsOutput.SendMessageW(mes);
    }
    else {
        //info->itsOutput.SendBuffer(buf);
    }
    return true;
}

MXDeviceManager* staticManager = NULL;

int receiverCnt = 0;

void receiver(MidiInPort port, IMidiMessageReceivedEventArgs args)
{
    IMidiMessage message = args.Message();
    PortInformation* info = nullptr;
    for (int i = 0; i < inputArray.size(); ++i) {
        PortInformation* test = inputArray[i];
        if (test != nullptr && test->itsInput == port) {
            info = test;
        }
    }

    if (info == nullptr) {
        return;
    }

    IBuffer buf = message.RawData();
    if (buf.Length() <= 3)
    {
        uint8_t* ptr = buf.data();
        uint8_t status = (buf.Length() >= 1) ? ptr[0] : 0;
        uint8_t data1 = (buf.Length() >= 2) ? ptr[1] : 0;
        uint8_t data2 = (buf.Length() >= 3) ? ptr[2] : 0;

        uint32_t message = (((status << 8) + data1) << 8) + data2;

        refCallShortMessage(info->number, message);
    }
    else {
        jbyte* ptr = (jbyte*)buf.data();

        JNIEnv* env2 = nullptr;
        _javaVM->AttachCurrentThread((void**)&env2, nullptr);
        jbyteArray jbyte = env2->NewByteArray(buf.Length());
        env2->SetByteArrayRegion(jbyte, 0, buf.Length(), ptr);
        refCallLongMessage(info->number, jbyte);
    }
}

extern MXDeviceManager *staticManager;
extern jmethodID cbCallText, cbCallShortMessage, cbCallLongMessage, cbDeviceListed;

void refCallText(const jchar* text) {
    JNIEnv* env2 = nullptr;
    _javaVM->AttachCurrentThread((void**)&env2, nullptr);

    jstring str = env2->NewString((uint16_t*)text, wcslen((const wchar_t*)text));
    env2->CallStaticVoidMethod(_javaClass, cbCallText, str);
}

void refCallShortMessage(jint device, jint message) {
    JNIEnv* env2 = nullptr;
    _javaVM->AttachCurrentThread((void**)&env2, nullptr);

    env2->CallStaticVoidMethod(_javaClass, cbCallShortMessage, device, message);
}

void refCallLongMessage(jint device, const jbyteArray data) {
    JNIEnv* env2 = nullptr;
    _javaVM->AttachCurrentThread((void**)&env2, nullptr);

    env2->CallStaticVoidMethod(_javaClass, cbCallLongMessage, device, data);
}


void refCallDeviceListed() {
    JNIEnv* env2 = nullptr;
    _javaVM->AttachCurrentThread((void**)&env2, nullptr);
    env2->CallStaticVoidMethod(_javaClass, cbDeviceListed);
}
