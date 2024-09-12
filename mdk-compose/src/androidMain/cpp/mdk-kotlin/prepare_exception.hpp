#ifndef MDK_COMPOSE_PREPARE_EXCEPTION_HPP
#define MDK_COMPOSE_PREPARE_EXCEPTION_HPP

#include <exception>

class PrepareException : public std::exception {
public:
    explicit PrepareException(int64_t code) {
        this->code = code;
    }
    int64_t code;
};


#endif //MDK_COMPOSE_PREPARE_EXCEPTION_HPP
