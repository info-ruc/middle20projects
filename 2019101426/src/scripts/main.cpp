#include "db.h"
#include "utils.h"
#include <cstdio>
#include "apue_db.h"
#include <iostream>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <string.h>


using namespace std;
int main(int argc, char* argv[]) {
    char                 *cmd;
    struct sigaction     sa;

    if ((cmd = strrchr(argv[0], '/')) == NULL)
        cmd = argv[0];
    else
        cmd++;

    log(0, "daemonize begin~");

    daemonize(cmd);

    log(0, "daemonize successfully~\n");

    return 0;
}
