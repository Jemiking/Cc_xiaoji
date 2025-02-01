import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing, typography, globalStyles } from '../../styles/theme';

type AccountRecord = {
  id: string;
  amount: number;
  description: string;
  date: string;
};

export const AccountingScreen: React.FC = () => {
  const [amount, setAmount] = useState('0');
  const [description, setDescription] = useState('');
  const [records, setRecords] = useState<AccountRecord[]>([]);

  const adjustAmount = (delta: number) => {
    const newAmount = (parseFloat(amount) + delta).toFixed(2);
    setAmount(newAmount);
  };

  const addRecord = () => {
    if (parseFloat(amount) === 0 || !description) return;

    const newRecord: AccountRecord = {
      id: Date.now().toString(),
      amount: parseFloat(amount),
      description,
      date: new Date().toLocaleDateString(),
    };

    setRecords([newRecord, ...records]);
    setAmount('0');
    setDescription('');
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.content}>
        <View style={styles.inputContainer}>
          <View style={styles.amountContainer}>
            <TextInput
              style={[styles.input, styles.amountInput]}
              value={amount}
              onChangeText={setAmount}
              keyboardType="numeric"
              placeholder="0.00"
            />
            <View style={styles.amountButtons}>
              <TouchableOpacity
                style={styles.adjustButton}
                onPress={() => adjustAmount(1)}
              >
                <Text style={styles.adjustButtonText}>+</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.adjustButton}
                onPress={() => adjustAmount(-1)}
              >
                <Text style={styles.adjustButtonText}>−</Text>
              </TouchableOpacity>
            </View>
          </View>
          <TextInput
            style={styles.input}
            value={description}
            onChangeText={setDescription}
            placeholder="添加描述..."
          />
          <TouchableOpacity
            style={[styles.button, styles.addButton]}
            onPress={addRecord}
          >
            <Text style={styles.buttonText}>添加记录</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.recordsContainer}>
          <Text style={styles.title}>记录历史</Text>
          {records.map((record) => (
            <View key={record.id} style={styles.recordCard}>
              <View style={styles.recordHeader}>
                <Text style={styles.amount}>
                  ¥ {record.amount.toFixed(2)}
                </Text>
                <Text style={styles.date}>{record.date}</Text>
              </View>
              <Text style={styles.description}>{record.description}</Text>
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    ...globalStyles.container,
  },
  content: {
    flex: 1,
    padding: spacing.md,
  },
  inputContainer: {
    gap: spacing.md,
    marginBottom: spacing.lg,
  },
  amountContainer: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  input: {
    ...globalStyles.input,
    flex: 1,
  },
  amountInput: {
    textAlign: 'right',
    fontSize: typography.sizes.large,
  },
  amountButtons: {
    gap: spacing.sm,
  },
  adjustButton: {
    ...globalStyles.button,
    width: 40,
    height: 40,
    padding: 0,
  },
  adjustButtonText: {
    ...globalStyles.buttonText,
    fontSize: typography.sizes.large,
  },
  button: {
    ...globalStyles.button,
  },
  addButton: {
    marginTop: spacing.sm,
  },
  buttonText: {
    ...globalStyles.buttonText,
  },
  recordsContainer: {
    gap: spacing.md,
  },
  title: {
    ...globalStyles.title,
  },
  recordCard: {
    ...globalStyles.card,
  },
  recordHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  amount: {
    fontSize: typography.sizes.medium,
    fontWeight: 'bold',
    color: colors.text,
  },
  date: {
    fontSize: typography.sizes.small,
    color: colors.textLight,
  },
  description: {
    fontSize: typography.sizes.regular,
    color: colors.text,
  },
}); 