#!/bin/bash
# Testing for Cloud Computing

# Estimated Program time
total_t=5

for i in {1..4}; do
start_t=`date +%s`
# Run the code
java -jar Commissar.jar pics$i out$i ice12.ee.cooper.edu 8888
# Backup the log files for later viewing
mv logs/protocol.log logs/protocol.log.test$i
mv logs/commissar.log logs/commissar.log.test$i
# Clear out the outputs
rm -r out$i
# See how long it took, so that we can synchronize test threads
cur_t=`date +%s`
let diff_t=( $start_t+$total_t-$cur_t )
if [ $diff_t -ge 0 ]; then
    echo "Sleeping for $diff_t seconds..."
    sleep $diff_t
else
    echo "Error: Program takes longer than $total_t seconds!"
fi
done
