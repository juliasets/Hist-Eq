Hist-Eq
=======

2-11-14 class for histogram equalization

The nominal purpose of this project is to enhance images.

The real purpose of this project is to create a distributed computing platform
that can take jobs from clients and distribute these jobs between workers
fairly. All the workers ("comrades") are equal, and there is no central
server for distributing the jobs. Instead, each comrade has a complete list of
all other comrades. Periodically, each comrade chooses another comrade randomly
and tells that comrade its status (that it is online, how busy it is, etc).
Subsequently, the comrade randomly chooses another comrade randomly and
asks it for all the most recent status information it has on the remaining
comrades. With such a protocol, each comrade is only contacted an average of
twice in each period, and status information (including news of a new or dead
node) propagates quickly to all nodes in a small number of periods.

The clients ("commisars") also know about every comrade and the status of
each. Each commisar periodically chooses one comrade randomly and asks it for
the status of all comrades. When the commisar wants to assign a job to the
computing platform, it chooses a comrade using a random selection process
weighted by the relative workloads of the different comrades. This way
load is distributed fairly between the different comrades. Meanwhile, the
commisar waits for its work to be done for it.

