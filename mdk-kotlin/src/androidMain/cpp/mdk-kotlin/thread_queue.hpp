#include <iostream>
#include <thread>
#include <condition_variable>
#include <queue>
#include <mdk/Player.h>

#ifndef MDK_COMPOSE_THREAD_QUEUE_HPP
#define MDK_COMPOSE_THREAD_QUEUE_HPP

template <typename T>
class ThreadQueue {

public:
    ThreadQueue(mdk::CallbackToken* token);
    ~ThreadQueue();
    void push(T value);
    T pop();
    CallbackToken* token;

private:
    std::queue<T> queue;
    std::mutex mutex;
    std::condition_variable conditionVariable;
};

template<typename T>
ThreadQueue<T>::~ThreadQueue() {
    this->token = nullptr;
}

template<typename T>
ThreadQueue<T>::ThreadQueue(CallbackToken* token) {
    this->token = token;
}

template<typename T>
T ThreadQueue<T>::pop() {
    std::unique_lock<std::mutex> lock(this->mutex);
    conditionVariable.wait(lock, [this] { return !this->queue.empty(); });
    T value = std::move(this->queue.front());
    this->queue.pop();
    return value;
}

template<typename T>
void ThreadQueue<T>::push(T value) {
    std::lock_guard<std::mutex> lock(this->mutex);
    this->queue.push(std::move(value));
    conditionVariable.notify_one();
}


#endif //MDK_COMPOSE_THREAD_QUEUE_HPP
