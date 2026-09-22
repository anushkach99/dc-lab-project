# Test Distributed Job Execution
$job = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs" -Method Post -ContentType "application/json" -Body '{"numTasks": 15, "complexity": "LOW"}'
$jobId = $job.jobId
Write-Host "Created Job: $jobId" -ForegroundColor Cyan

Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/$jobId/start" -Method Post | Out-Null
Write-Host "Job started. Waiting 4 seconds for execution across workers..." -ForegroundColor Yellow
Start-Sleep -Seconds 4

Write-Host "`n--- JOB PROGRESS ---" -ForegroundColor Green
$resJob = Invoke-RestMethod -Uri "http://localhost:8080/api/jobs/$jobId"
Write-Host "Total Tasks     : $($resJob.totalTasks)"
Write-Host "Completed Tasks : $($resJob.completedTasks)"
Write-Host "Running Tasks   : $($resJob.runningTasks)"
Write-Host "Pending Tasks   : $($resJob.pendingTasks)"
Write-Host "Status          : $($resJob.status)"

Write-Host "`n--- WORKER PROGRESS ---" -ForegroundColor Green
$workers = Invoke-RestMethod -Uri "http://localhost:8080/api/workers"
foreach ($w in $workers) {
    Write-Host "Worker $($w.workerId) [$($w.nodeType)] -> Completed: $($w.completedTasks), Throughput: $($w.throughput) t/s, CPU: $($w.cpuUtilization)%"
}

Write-Host "`n--- LAMPORT EVENTS ---" -ForegroundColor Green
$events = Invoke-RestMethod -Uri "http://localhost:8080/api/events"
$events | Select-Object -Last 5 | ForEach-Object {
    Write-Host "LC: $($_.lamportTimestamp) | Node: $($_.nodeId) | Type: $($_.eventType) | $($_.description)"
}
