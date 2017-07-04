# Fluvial

## What's Fluvial for?
This is a framework for dealing with jobs. Assign available performer for specific job automatically. A very typical use case
is to make robots process tasks. Job can be in a complex data structure, currently tree data structure, which means each job
could have its own sub jobs.

## What Fluvial has done for you?
1. Easy way to generate job instance with defined hierarchy.
2. Scheduler to handle each job.
3. Allocator to decide how to assign performer for the job.
4. Storage support to dealing with optimistic/pessimistic lock, transaction, so the database would always be sync between
scheduler and user operation.
5. Communication support for tcp / websocket.
6. Implement the goal mechanism, so each job would continue execute until it meets all goals.

## What you need to do if using Fluvial?