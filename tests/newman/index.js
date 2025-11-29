/**
 * Newman Test Runner
 * Execute Integration Tests (POST, GET, PUT, DELETE) with dataset iterations
 *
 * Usage:
 *   node index.js --collection ./collection.json --environment ./env.json --data ./dataset.json
 */

const newman = require('newman');
const argv = require('minimist')(process.argv.slice(2));
const fs = require('fs');
const path = require('path');

// Parse arguments
const collectionPath = argv.collection || argv.c || './collection.json';
const envPath = argv.environment || argv.env || argv.e || './env.json';
const dataPath = argv.data || argv.d || './dataset.json';
const reporters = argv.reporters || 'cli,json,html';
const outputDir = argv.output || './newman-results';

console.log('═══════════════════════════════════════════════════════');
console.log('🚀 Newman Integration Test Runner');
console.log('═══════════════════════════════════════════════════════');
console.log(`📁 Collection: ${collectionPath}`);
console.log(`🌍 Environment: ${envPath}`);
console.log(`📊 Dataset: ${dataPath}`);
console.log(`📝 Reporters: ${reporters}`);
console.log('═══════════════════════════════════════════════════════\n');

// Validate collection file
if (!fs.existsSync(collectionPath)) {
  console.error('❌ Collection file not found:', collectionPath);
  process.exit(1);
}

// Create output directory if needed
if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

// Build Newman options
const opts = {
  collection: require(path.resolve(collectionPath)),
  reporters: reporters.split(','),
  reporter: {
    htmlextra: {
      export: path.join(outputDir, 'newman-report.html')
    },
    json: {
      export: path.join(outputDir, 'newman-report.json')
    }
  },
  insecure: true, // Ignore SSL errors for local testing
  timeout: 30000, // 30 seconds timeout
  iterationCount: 1
};

// Add environment if exists
if (fs.existsSync(envPath)) {
  console.log('✅ Loading environment file...');
  opts.environment = require(path.resolve(envPath));
} else {
  console.warn('⚠️  Environment file not found, continuing without it');
}

// Add dataset for data-driven testing (POST, GET, PUT, DELETE iterations)
if (fs.existsSync(dataPath)) {
  console.log('✅ Loading dataset file for data-driven testing...');
  opts.iterationData = path.resolve(dataPath);

  // Count iterations
  const dataContent = fs.readFileSync(dataPath, 'utf8');
  try {
    const data = JSON.parse(dataContent);
    if (Array.isArray(data)) {
      opts.iterationCount = data.length;
      console.log(`📊 Dataset loaded: ${data.length} iterations will be executed`);
    }
  } catch (e) {
    console.warn('⚠️  Could not parse dataset, using default iteration count');
  }
} else {
  console.warn('⚠️  Dataset file not found, running single iteration');
}

console.log('\n🏃 Starting Newman test execution...\n');

// Run Newman
newman.run(opts, function (err, summary) {
  console.log('\n═══════════════════════════════════════════════════════');

  if (err) {
    console.error('❌ Newman execution error:', err);
    console.log('═══════════════════════════════════════════════════════\n');
    process.exit(2);
  }

  // Display summary
  console.log('📊 Test Execution Summary:');
  console.log('═══════════════════════════════════════════════════════');
  console.log(`Total requests: ${summary.run.stats.requests.total}`);
  console.log(`Total assertions: ${summary.run.stats.assertions.total}`);
  console.log(`Failed assertions: ${summary.run.stats.assertions.failed}`);
  console.log(`Iterations: ${summary.run.stats.iterations.total}`);

  const failures = summary.run.failures || [];
  const failureCount = failures.length;

  if (failureCount > 0) {
    console.log(`\n❌ Test failures: ${failureCount}`);
    console.log('───────────────────────────────────────────────────────');

    failures.forEach((failure, index) => {
      console.log(`\n${index + 1}. ${failure.error.name}`);
      console.log(`   Source: ${failure.source?.name || 'Unknown'}`);
      console.log(`   Message: ${failure.error.message}`);
      if (failure.error.test) {
        console.log(`   Test: ${failure.error.test}`);
      }
    });

    console.log('\n═══════════════════════════════════════════════════════');
    console.log('❌ Integration tests FAILED');
    console.log('═══════════════════════════════════════════════════════\n');
    process.exit(3);
  }

  console.log('\n✅ All integration tests PASSED');
  console.log('═══════════════════════════════════════════════════════');
  console.log(`📁 Reports saved to: ${outputDir}`);
  console.log('═══════════════════════════════════════════════════════\n');

  process.exit(0);
});
