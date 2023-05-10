#include "pch.h"
#include "NoteMessage.h"
#include "boost/lockfree/queue.hpp"

boost::lockfree::queue<Event*> _note_vector;
int newNote = 0;

void setEventRecycled(Event* bin) {
    if (bin->type == Event::EventTypes::kDataEvent) {
        delete bin->data.bytes;
        bin->data.bytes = nullptr;
    }
    while (!_note_vector.push(bin)) {

    }
}

Event* useEventRecycled() {
    if (_note_vector.empty()) {
        newNote++;
        return new Event();
    }
    Event* ret;
    _note_vector.pop(ret);
    *ret = {};
    return ret;
}

